package ch.giantific.qwittig.data.parse;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 31.12.14.
 */
public class LocalQuery {

    private static final String LOG_TAG = LocalQuery.class.getSimpleName();

    private LocalQuery() {
        // Class cannot be instantiated
    }

    public static void queryPurchases(final PurchaseLocalQueryListener listener) {
        final Group group = getCurrentGroup();

        ParseQuery<ParseObject> query = getPurchasesQuery();
        query.whereEqualTo(Purchase.GROUP, group);
        query.whereDoesNotExist(Purchase.DRAFT_ID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "queryPurchases " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onPurchasesLocalQueried(parseObjects);
                }
            }
        });
    }

    private static ParseQuery<ParseObject> getPurchasesQuery() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Purchase.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Purchase.ITEMS);
        query.include(Purchase.BUYER);
        query.include(Purchase.USERS_INVOLVED);
        query.orderByDescending(Purchase.DATE);
        return query;
    }

    public static void queryPurchase(String purchaseId, final ObjectLocalFetchListener listener) {
        ParseQuery<ParseObject> query = getPurchasesQuery();
        query.getInBackground(purchaseId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "queryPurchase " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onObjectFetched(parseObject);
                }
            }
        });
    }

    public static void queryUsers(final UserLocalQueryListener listener) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.fromLocalDatastore();
        query.whereEqualTo(User.GROUPS, getCurrentGroup());
        query.whereEqualTo(User.IS_DELETED, false);
        query.ignoreACLs();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "queryUsers " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onUsersLocalQueried(parseUsers);
                }
            }
        });
    }

    public static void queryDrafts(final PurchaseLocalQueryListener listener) {
        ParseQuery<ParseObject> query = getDraftsQuery();
        query.whereEqualTo(Purchase.BUYER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "queryDrafts " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onPurchasesLocalQueried(parseObjects);
                }
            }
        });
    }

    private static ParseQuery<ParseObject> getDraftsQuery() {
        ParseQuery<ParseObject> query = getPurchasesQuery();
        query.whereEqualTo(Purchase.GROUP, getCurrentGroup());
        query.whereExists(Purchase.DRAFT_ID);
        return query;
    }

    public static void queryDraft(final ObjectLocalFetchListener listener, String draftId) {
        ParseQuery<ParseObject> query = getDraftsQuery();
        query.whereEqualTo(Purchase.DRAFT_ID, draftId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "queryDraft " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onObjectFetched(parseObject);
                }
            }
        });
    }

    public static void queryCompensationsUnpaid(final CompensationLocalQueryListener listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Compensation.GROUP, getCurrentGroup());
        query.whereEqualTo(Compensation.IS_PAID, false);
        query.orderByAscending(OnlineQuery.DATE_CREATED);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "queryCompensationsUnpaid " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onCompensationsLocalQueried(parseObjects);
                }
            }
        });
    }

    public static void queryCompensationsPaid(final CompensationLocalQueryListener listener) {
        User currentUser = (User) ParseUser.getCurrentUser();

        ParseQuery<ParseObject> payerQuery = ParseQuery.getQuery(Compensation.CLASS);
        payerQuery.whereEqualTo(Compensation.PAYER, currentUser);

        ParseQuery<ParseObject> beneficiaryQuery = ParseQuery.getQuery(Compensation.CLASS);
        beneficiaryQuery.whereEqualTo(Compensation.BENEFICIARY, currentUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(payerQuery);
        queries.add(beneficiaryQuery);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.fromLocalDatastore();
        query.whereEqualTo(Compensation.GROUP, getCurrentGroup());
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.ignoreACLs();
        query.orderByDescending(OnlineQuery.DATE_UPDATED);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "queryCompensationsPaid " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onCompensationsLocalQueried(parseObjects);
                }
            }
        });
    }

    public static void fetchObjectData(final ObjectLocalFetchListener listener,
                                       final ParseObject parseObjectToFetch) {
        parseObjectToFetch.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    parseObjectToFetch.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                listener.onObjectFetched(parseObject);
                            }
                        }
                    });
                    return;
                }

                listener.onObjectFetched(parseObject);
            }
        });
    }

    public static void fetchObjectFromId(final ObjectLocalFetchListener listener,
                                         String objectType, String objectId) {
        ParseObject parseObject = ParseObject.createWithoutData(objectType, objectId);
        parseObject.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "fetchObject " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onObjectFetched(parseObject);
                }
            }
        });
    }

    private static Group getCurrentGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser == null) {
            return null;
        }

        return currentUser.getCurrentGroup();
    }

    public interface PurchaseLocalQueryListener {
        void onPurchasesLocalQueried(List<ParseObject> purchases);
    }

    public interface CompensationLocalQueryListener {
        void onCompensationsLocalQueried(List<ParseObject> compensations);
    }

    public interface UserLocalQueryListener {
        void onUsersLocalQueried(List<ParseUser> users);
    }

    public interface ObjectLocalFetchListener {
        void onObjectFetched(ParseObject object);
    }
}
