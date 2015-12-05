/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;

/**
 * Provides an implementation of {@link PurchaseRepository} that uses the Parse.com framework as
 * the local and online data store.
 */
public class ParsePurchaseRepository extends ParseGenericRepository implements PurchaseRepository {

    private static final String LOG_TAG = ParsePurchaseRepository.class.getSimpleName();

    public ParsePurchaseRepository(Context context) {
        super(context);
    }

    @Override
    public void getPurchasesLocalAsync(@NonNull User currentUser, boolean getDrafts,
                                       @NonNull final GetPurchasesLocalListener listener) {
        ParseQuery<ParseObject> query = getPurchasesLocalQuery();
        query.whereEqualTo(Purchase.GROUP, currentUser.getCurrentGroup());
        if (getDrafts) {
            query.whereExists(Purchase.DRAFT_ID);
            query.whereEqualTo(Purchase.BUYER, currentUser);
        } else {
            query.whereDoesNotExist(Purchase.DRAFT_ID);
        }
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e == null) {
                    listener.onPurchasesLocalLoaded(parseObjects);
                }
            }
        });
    }

    @Override
    public void getPurchaseLocalAsync(@NonNull String purchaseId, boolean isDraft,
                                      @NonNull final GetPurchaseLocalListener listener) {
        ParseQuery<ParseObject> query = getPurchasesLocalQuery();
        if (isDraft) {
            query.whereEqualTo(Purchase.DRAFT_ID, purchaseId);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, @Nullable ParseException e) {
                    if (e == null) {
                        listener.onPurchaseLocalLoaded((Purchase) parseObject);
                    }
                }
            });
        } else {
            query.getInBackground(purchaseId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, @Nullable ParseException e) {
                    if (e == null) {
                        listener.onPurchaseLocalLoaded((Purchase) parseObject);
                    }
                }
            });
        }
    }

    @NonNull
    private ParseQuery<ParseObject> getPurchasesLocalQuery() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Purchase.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.USERS_INVOLVED);
        query.orderByDescending(Purchase.DATE);
        return query;
    }

    @Override
    public void fetchPurchaseDataLocalAsync(@NonNull String purchaseId,
                                            @NonNull final GetPurchaseLocalListener listener) {
        ParseObject parseObject = ParseObject.createWithoutData(Purchase.CLASS, purchaseId);
        parseObject.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, @Nullable ParseException e) {
                if (e == null) {
                    listener.onPurchaseLocalLoaded((Purchase) parseObject);
                }
            }
        });
    }

    @Override
    public boolean removePurchaseLocal(@NonNull String purchaseId, @NonNull String groupId) {
        ParseObject purchase = ParseObject.createWithoutData(Purchase.CLASS, purchaseId);
        try {
            purchase.unpin(Purchase.PIN_LABEL + groupId);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public void updatePurchasesAsync(@NonNull User currentUser, @NonNull List<ParseObject> groups,
                                     @NonNull final String currentGroupId,
                                     @NonNull final UpdatePurchasesListener listener) {
        mNumberOfUpdatedQueries = groups.size();

        for (final ParseObject group : groups) {
            ParseQuery<ParseObject> query = getPurchasesOnlineQuery(currentUser);
            query.whereEqualTo(Purchase.GROUP, group);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                    if (e != null) {
                        listener.onPurchaseUpdateFailed(ParseErrorHandler.handleParseError(mContext, e));
                        return;
                    }

                    final String groupId = group.getObjectId();
                    final String label = Purchase.PIN_LABEL + groupId;
                    ParseObject.unpinAllInBackground(label, new DeleteCallback() {
                        @Override
                        public void done(@Nullable ParseException e) {
                            if (e != null) {
                                listener.onPurchaseUpdateFailed(ParseErrorHandler.handleParseError(mContext, e));
                                return;
                            }

                            ParseObject.pinAllInBackground(label, parseObjects, new SaveCallback() {
                                @Override
                                public void done(@Nullable ParseException e) {
                                    if (e != null) {
                                        listener.onPurchaseUpdateFailed(ParseErrorHandler.handleParseError(mContext, e));
                                        return;
                                    }

                                    final boolean isCurrentGroup =
                                            groupId.equals(currentGroupId);
                                    listener.onPurchasesUpdated(isCurrentGroup);

                                    if (allUpdatesDone()) {
                                        listener.onAllPurchasesUpdated();
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    @NonNull
    private ParseQuery<ParseObject> getPurchasesOnlineQuery(@NonNull User currentUser) {
        ParseQuery<ParseObject> buyerQuery = ParseQuery.getQuery(Purchase.CLASS);
        buyerQuery.whereEqualTo(Purchase.BUYER, currentUser);

        ParseQuery<ParseObject> involvedQuery = ParseQuery.getQuery(Purchase.CLASS);
        involvedQuery.whereEqualTo(Purchase.USERS_INVOLVED, currentUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(buyerQuery);
        queries.add(involvedQuery);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.USERS_INVOLVED);
        query.setLimit(QUERY_ITEMS_PER_PAGE);
        query.orderByDescending(Purchase.DATE);
        return query;
    }

    @Override
    public void getPurchasesOnlineAsync(@NonNull User currentUser, @NonNull final Group group, int skip,
                                        @NonNull final GetPurchasesOnlineListener listener) {
        ParseQuery<ParseObject> query = getPurchasesOnlineQuery(currentUser);
        query.setSkip(skip);
        query.whereEqualTo(Purchase.GROUP, group);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    listener.onPurchaseOnlineLoadFailed(ParseErrorHandler.handleParseError(mContext, e));
                    return;
                }

                ParseObject.pinAllInBackground(Purchase.PIN_LABEL + group.getObjectId(),
                        parseObjects, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e == null) {
                                    listener.onPurchasesOnlineLoaded(parseObjects);
                                }
                            }
                        });
            }
        });
    }

    @Override
    public boolean updatePurchases(@NonNull User currentUser, @NonNull List<ParseObject> groups) {
        if (groups.isEmpty()) {
            return false;
        }

        for (ParseObject group : groups) {
            try {
                List<ParseObject> purchases = getPurchasesForGroupOnline(currentUser, group);
                final String groupId = group.getObjectId();
                final String label = Purchase.PIN_LABEL + groupId;

                ParseObject.unpinAll(label);
                ParseObject.pinAll(label, purchases);
            } catch (ParseException e) {
                return false;
            }
        }

        return true;
    }

    private List<ParseObject> getPurchasesForGroupOnline(@NonNull User currentUser,
                                                         @NonNull ParseObject group)
            throws ParseException {
        ParseQuery<ParseObject> query = getPurchasesOnlineQuery(currentUser);
        query.whereEqualTo(Purchase.GROUP, group);
        return query.find();
    }

    @Override
    public boolean updatePurchase(@NonNull String purchaseId, boolean isNew) {
        try {
            Purchase purchase = getPurchaseOnline(purchaseId);
            final String groupId = purchase.getGroup().getObjectId();
            final String pinLabel = Purchase.PIN_LABEL + groupId;

            if (isNew) {
                purchase.pin(pinLabel);
            } else {
                // although we only update an existing purchase, we need to unpin and repin it
                // because the items have changed
                purchase.unpin(pinLabel);
                purchase.pin(pinLabel);
            }
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private Purchase getPurchaseOnline(@NonNull String objectId) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Purchase.CLASS);
        query.include(Purchase.ITEMS);
        query.include(Purchase.USERS_INVOLVED);
        query.include(Purchase.BUYER);
        return (Purchase) query.get(objectId);
    }
}
