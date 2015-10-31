/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;

/**
 * Provides an implementation of {@link CompensationRepository} that uses the Parse.com framework
 * as the local and online data store.
 */
public class ParseCompensationRepository extends ParseGenericRepository implements CompensationRepository {

    private static final String DATE_CREATED = "createdAt";
    private static final String DATE_UPDATED = "updatedAt";
    private static final String LOG_TAG = ParseCompensationRepository.class.getSimpleName();

    @Override
    public void getCompensationsLocalUnpaidAsync(@NonNull Group group,
                                                 @NonNull final GetCompensationsLocalListener listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Compensation.GROUP, group);
        query.whereEqualTo(Compensation.IS_PAID, false);
        query.orderByAscending(DATE_CREATED);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e == null) {
                    listener.onCompensationsLocalLoaded(parseObjects);
                }
            }
        });
    }

    @Override
    public void getCompensationsLocalPaidAsync(@NonNull User currentUser, @NonNull Group group,
                                               @NonNull final GetCompensationsLocalListener listener) {
        ParseQuery<ParseObject> payerQuery = ParseQuery.getQuery(Compensation.CLASS);
        payerQuery.whereEqualTo(Compensation.PAYER, currentUser);

        ParseQuery<ParseObject> beneficiaryQuery = ParseQuery.getQuery(Compensation.CLASS);
        beneficiaryQuery.whereEqualTo(Compensation.BENEFICIARY, currentUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(payerQuery);
        queries.add(beneficiaryQuery);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.fromLocalDatastore();
        query.whereEqualTo(Compensation.GROUP, group);
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.ignoreACLs();
        query.orderByDescending(DATE_UPDATED);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e == null) {
                    listener.onCompensationsLocalLoaded(parseObjects);
                }
            }
        });
    }

    @Override
    public boolean removeCompensationLocal(@NonNull String compensationId) {
        ParseObject compensation = ParseObject.createWithoutData(Compensation.CLASS, compensationId);
        try {
            compensation.unpin(Compensation.PIN_LABEL_UNPAID);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    @Override
    public void updateCompensationsUnpaidAsync(@NonNull List<ParseObject> groups,
                                               @NonNull final UpdateCompensationsListener listener) {
        ParseQuery<ParseObject> query = getCompensationsOnlineQuery();
        query.whereContainedIn(Compensation.GROUP, groups);
        query.whereEqualTo(Compensation.IS_PAID, false);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    listener.onCompensationUpdateFailed(e.getCode());
                    return;
                }

                ParseObject.unpinAllInBackground(Compensation.PIN_LABEL_UNPAID, new DeleteCallback() {
                    @Override
                    public void done(@Nullable ParseException e) {
                        if (e != null) {
                            listener.onCompensationUpdateFailed(e.getCode());
                            return;
                        }

                        ParseObject.pinAllInBackground(Compensation.PIN_LABEL_UNPAID, parseObjects, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e == null) {
                                    listener.onCompensationsUnpaidUpdated();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void updateCompensationsPaidAsync(@NonNull List<ParseObject> groups,
                                             @NonNull final String currentGroupId,
                                             @NonNull final UpdateCompensationsListener listener) {
        int groupsSize = groups.size();
        if (groupsSize == 0) {
            listener.onCompensationUpdateFailed(0);
            return;
        }

        mNumberOfUpdatedQueries = groupsSize;
        for (final ParseObject group : groups) {
            ParseQuery<ParseObject> query = getCompensationsOnlineQuery();
            query.whereEqualTo(Compensation.GROUP, group);
            query.whereEqualTo(Compensation.IS_PAID, true);
            query.setLimit(ParseGenericRepository.QUERY_ITEMS_PER_PAGE);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                    if (e != null) {
                        listener.onCompensationUpdateFailed(e.getCode());
                        return;
                    }

                    final String groupId = group.getObjectId();
                    final String pinLabel = Compensation.PIN_LABEL_PAID + groupId;

                    ParseObject.unpinAllInBackground(pinLabel, new DeleteCallback() {
                        public void done(@Nullable ParseException e) {
                            if (e != null) {
                                listener.onCompensationUpdateFailed(e.getCode());
                                return;
                            }

                            ParseObject.pinAllInBackground(pinLabel, parseObjects, new SaveCallback() {
                                @Override
                                public void done(@Nullable ParseException e) {
                                    if (e != null) {
                                        listener.onCompensationUpdateFailed(e.getCode());
                                        return;
                                    }

                                    final boolean isCurrentGroup =
                                            groupId.equals(currentGroupId);
                                    listener.onCompensationsPaidUpdated(isCurrentGroup);

                                    if (allUpdatesDone()) {
                                        listener.onAllCompensationsPaidUpdated();
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    @Override
    public void getCompensationsPaidOnlineAsync(@NonNull final Group group, int skip,
                                                @NonNull final GetCompensationsOnlineListener listener) {
        ParseQuery<ParseObject> query = getCompensationsOnlineQuery();
        query.setSkip(skip);
        query.whereEqualTo(Compensation.GROUP, group);
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    listener.onCompensationsPaidOnlineLoadFailed(e.getCode());
                    return;
                }

                ParseObject.pinAllInBackground(Compensation.PIN_LABEL_PAID + group.getObjectId(),
                        parseObjects, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e == null) {
                                    listener.onCompensationsPaidOnlineLoaded(parseObjects);
                                }
                            }
                        });
            }
        });
    }

    @Override
    public boolean updateCompensations(@NonNull List<ParseObject> groups) {
        if (groups.isEmpty()) {
            return false;
        }

        try {
            updateCompensationsUnpaid(groups);
            updateCompensationsPaid(groups);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private void updateCompensationsUnpaid(@NonNull List<ParseObject> groups) throws ParseException {
        ParseQuery<ParseObject> query = getCompensationsOnlineQuery();
        query.whereContainedIn(Compensation.GROUP, groups);
        query.whereEqualTo(Compensation.IS_PAID, false);

        List<ParseObject> compensationsUnpaid = query.find();
        ParseObject.unpinAll(Compensation.PIN_LABEL_UNPAID);
        ParseObject.pinAll(Compensation.PIN_LABEL_UNPAID, compensationsUnpaid);
    }

    private void updateCompensationsPaid(@NonNull final List<ParseObject> groups) throws ParseException {
        for (ParseObject group : groups) {
            ParseQuery<ParseObject> query = getCompensationsOnlineQuery();
            query.whereEqualTo(Compensation.GROUP, group);
            query.whereEqualTo(Compensation.IS_PAID, true);
            query.setLimit(ParseGenericRepository.QUERY_ITEMS_PER_PAGE);

            List<ParseObject> compensationsPaid = query.find();
            final String groupId = group.getObjectId();
            final String pinLabel = Compensation.PIN_LABEL_PAID + groupId;
            ParseObject.unpinAll(pinLabel);
            ParseObject.pinAll(pinLabel, compensationsPaid);
        }
    }

    @NonNull
    private ParseQuery<ParseObject> getCompensationsOnlineQuery() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        query.orderByDescending(DATE_CREATED);
        return query;
    }

    @Override
    @Nullable
    public Boolean updateCompensation(@NonNull String compensationId, boolean isNew) {
        try {
            Compensation compensation = getCompensationOnline(compensationId);
            boolean isPaid = compensation.isPaid();
            if (isNew) {
                String groupId = compensation.getGroup().getObjectId();
                String pinLabel;
                if (isPaid) {
                    pinLabel = Compensation.PIN_LABEL_PAID + groupId;
                } else {
                    pinLabel = Compensation.PIN_LABEL_UNPAID;
                }

                compensation.pin(pinLabel);
            } else if (isPaid) {
                compensation.unpin(Compensation.PIN_LABEL_UNPAID);

                String groupId = compensation.getGroup().getObjectId();
                String pinLabel = Compensation.PIN_LABEL_PAID + groupId;
                compensation.pin(pinLabel);
            }

            return isPaid;
        } catch (ParseException e) {
            return null;
        }
    }

    private Compensation getCompensationOnline(@NonNull String compensationId)
            throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        return (Compensation) query.get(compensationId);
    }

}
