/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.parse;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.Task;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Provides the static methods used to perform queries from the parse.com local data store.
 */
public class LocalQuery {

    private static final String LOG_TAG = LocalQuery.class.getSimpleName();

    private LocalQuery() {
        // Class cannot be instantiated
    }

    /**
     * Queries the local data store for purchases of the user's current group.
     *
     * @param listener the callback called when the query finishes
     */
    public static void queryPurchases(@Nullable final PurchaseLocalQueryListener listener) {
        final Group group = getCurrentGroup();

        ParseQuery<ParseObject> query = getPurchasesQuery();
        query.whereEqualTo(Purchase.GROUP, group);
        query.whereDoesNotExist(Purchase.DRAFT_ID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, @Nullable ParseException e) {
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

    @NonNull
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

    /**
     * Queries the local data store for a single purchase.
     *
     * @param purchaseId the object id of the purchase to query
     * @param listener   the callback called when the query finishes
     */
    public static void queryPurchase(@NonNull String purchaseId,
                                     @Nullable final ObjectLocalFetchListener listener) {
        ParseQuery<ParseObject> query = getPurchasesQuery();
        query.getInBackground(purchaseId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, @Nullable ParseException e) {
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

    /**
     * Queries the local data store for the users of the current group.
     *
     * @param listener the callback called when the query finishes
     */
    public static void queryUsers(@Nullable final UserLocalQueryListener listener) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.fromLocalDatastore();
        query.whereEqualTo(User.GROUPS, getCurrentGroup());
        query.whereEqualTo(User.IS_DELETED, false);
        query.ignoreACLs();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, @Nullable ParseException e) {
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

    /**
     * Queries the local data store for all the draft purchase object of the current group.
     *
     * @param listener the callback called when the query finishes
     */
    public static void queryDrafts(@Nullable final PurchaseLocalQueryListener listener) {
        ParseQuery<ParseObject> query = getDraftsQuery();
        query.whereEqualTo(Purchase.BUYER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, @Nullable ParseException e) {
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

    @NonNull
    private static ParseQuery<ParseObject> getDraftsQuery() {
        ParseQuery<ParseObject> query = getPurchasesQuery();
        query.whereEqualTo(Purchase.GROUP, getCurrentGroup());
        query.whereExists(Purchase.DRAFT_ID);
        return query;
    }

    /**
     * Queries the local data store for a single draft.
     *
     * @param draftId  the draft id of the purchase to query
     * @param listener the callback called when the query finishes
     */
    public static void queryDraft(@NonNull String draftId,
                                  @Nullable final ObjectLocalFetchListener listener) {
        ParseQuery<ParseObject> query = getDraftsQuery();
        query.whereEqualTo(Purchase.DRAFT_ID, draftId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, @Nullable ParseException e) {
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

    /**
     * Queries the local data store for unpaid compensations  of the user's current group.
     *
     * @param listener the callback called when the query finishes
     */
    public static void queryCompensationsUnpaid(@Nullable final CompensationLocalQueryListener listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Compensation.GROUP, getCurrentGroup());
        query.whereEqualTo(Compensation.IS_PAID, false);
        query.orderByAscending(OnlineQuery.DATE_CREATED);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, @Nullable ParseException e) {
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

    /**
     * Queries the local data store for paid compensations of the user's current group.
     *
     * @param listener the callback called when the query finishes
     */
    public static void queryCompensationsPaid(@Nullable final CompensationLocalQueryListener listener) {
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
            public void done(List<ParseObject> parseObjects, @Nullable ParseException e) {
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

    /**
     * Queries the local data store for tasks of the user's current group.
     *
     * @param listener the callback called when the query finishes
     */
    public static void queryTasks(@NonNull Date deadline,
                                  @Nullable final TaskLocalQueryListener listener) {
        ParseQuery<ParseObject> deadlineQuery = ParseQuery.getQuery(Task.CLASS);
        deadlineQuery.whereLessThan(Task.DEADLINE, deadline);

        ParseQuery<ParseObject> asNeededQuery = ParseQuery.getQuery(Task.CLASS);
        asNeededQuery.whereDoesNotExist(Task.DEADLINE);

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(deadlineQuery);
        queries.add(asNeededQuery);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.whereEqualTo(Task.GROUP, getCurrentGroup());
        query.include(Task.USERS_INVOLVED);
        query.orderByAscending(Task.DEADLINE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "queryTasks " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onTasksLocalQueried(parseObjects);
                }
            }
        });
    }

    /**
     * Queries the local data store for a single task.
     *
     * @param taskId   the object id of the task to query
     * @param listener the callback called when the query finishes
     */
    public static void queryTask(@NonNull String taskId,
                                 @Nullable final ObjectLocalFetchListener listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Task.CLASS);
        query.fromLocalDatastore();
        query.ignoreACLs();
        query.include(Task.USERS_INVOLVED);
        query.getInBackground(taskId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, @Nullable ParseException e) {
                if (e != null) {
                    Log.e(LOG_TAG, "queryTask " + e.toString());
                    return;
                }

                if (listener != null) {
                    listener.onObjectFetched(object);
                }
            }
        });
    }

    /**
     * Fetches the data of an object from the local data store. If there is no data available in
     * the local data store it will try to fetch the data online.
     *
     * @param parseObjectToFetch the object to fetch the data for
     * @param listener           the callback called when the query finishes
     */
    public static void fetchObjectData(@NonNull final ParseObject parseObjectToFetch,
                                       @NonNull final ObjectLocalFetchListener listener) {
        parseObjectToFetch.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, @Nullable ParseException e) {
                if (e != null) {
                    parseObjectToFetch.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, @Nullable ParseException e) {
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

    /**
     * Fetches the data of an object from the local data store.
     *
     * @param objectType the class type of the object
     * @param objectId   the object id
     * @param listener   the callback called when the query finishes
     */
    public static void fetchObjectFromId(@NonNull String objectType, @NonNull String objectId,
                                         @Nullable final ObjectLocalFetchListener listener) {
        ParseObject parseObject = ParseObject.createWithoutData(objectType, objectId);
        parseObject.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject parseObject, @Nullable ParseException e) {
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

    /**
     * Defines the callback when a purchase query finishes.
     */
    public interface PurchaseLocalQueryListener {
        /**
         * Called when a query finished without error.
         *
         * @param purchases the queried purchases
         */
        void onPurchasesLocalQueried(@NonNull List<ParseObject> purchases);
    }

    /**
     * Defines the callback when a compensation query finishes.
     */
    public interface CompensationLocalQueryListener {
        /**
         * Called when a query finished without error.
         *
         * @param compensations the queried compensations
         */
        void onCompensationsLocalQueried(@NonNull List<ParseObject> compensations);
    }

    /**
     * Defines the callback when a user query finishes.
     */
    public interface UserLocalQueryListener {
        /**
         * Called when a query finished without error.
         *
         * @param users the queried users
         */
        void onUsersLocalQueried(@NonNull List<ParseUser> users);
    }

    /**
     * Defines the callback when a task query finishes.
     */
    public interface TaskLocalQueryListener {
        /**
         * Called when a query finished without error.
         *
         * @param tasks the queried tasks
         */
        void onTasksLocalQueried(@NonNull List<ParseObject> tasks);
    }

    /**
     * Defines the callback when a generic object query finishes.
     */
    public interface ObjectLocalFetchListener {
        /**
         * Called when a query finished without error.
         *
         * @param object the queried object
         */
        void onObjectFetched(@NonNull ParseObject object);
    }
}
