package ch.giantific.qwittig.data.parse;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.ParseErrorHandler;

/**
 * Created by fabio on 31.12.14.
 */
public class OnlineQuery {

    public static final int QUERY_ITEMS_PER_PAGE = 15;
    public static final String DATE_CREATED = "createdAt";
    public static final String DATE_UPDATED = "updatedAt";
    public static final String INTENT_FILTER_DATA_NEW = "ch.giantific.qwittig.push.DATA_NEW";
    public static final String INTENT_DATA_TYPE = "intent_data_type";
    public static final String INTENT_COMPENSATION_PAID = "intent_compensation_paid";

    @IntDef({DATA_TYPE_PURCHASE, DATA_TYPE_USER, DATA_TYPE_COMPENSATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DataType {}
    public static final int DATA_TYPE_PURCHASE = 1;
    public static final int DATA_TYPE_USER = 2;
    public static final int DATA_TYPE_COMPENSATION = 3;

    private static final String LOG_TAG = OnlineQuery.class.getSimpleName();

    private OnlineQuery() {
        // Class cannot be instantiated
    }

    public static void queryPurchases(final Context context, final PurchasePinListener listener) {
        List<ParseObject> groups = getCurrentUserGroups();
        if (groups.isEmpty()) {
            return;
        }

        for (final ParseObject group : groups) {
            ParseQuery<ParseObject> query = getPurchasesQuery();
            query.whereEqualTo(Purchase.GROUP, group);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(final List<ParseObject> parseObjects, ParseException e) {
                    if (e != null) {
                        ParseErrorHandler.handleParseError(context, e);
                        if (listener != null) {
                            listener.onPurchasePinFailed(ParseErrorHandler.getErrorMessage(context, e));
                        }
                        return;
                    }

                    final String groupId = group.getObjectId();
                    final String label = Purchase.PIN_LABEL + groupId;

                    // Release any objects previously pinned for the purchase pin label
                    ParseObject.unpinAllInBackground(label, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                return;
                            }

                            // Add the latest results for this query to the cache
                            ParseObject.pinAllInBackground(label, parseObjects, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        notifyPurchaseChange(context, listener);
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private static ParseQuery<ParseObject> getPurchasesQuery() {
        User currentUser = (User) ParseUser.getCurrentUser();

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

    public static void queryPurchasesMore(final Context context,
                                          final PurchaseQueryListener listener, int skip) {
        final Group group = getCurrentGroup();
        if (group == null) {
            return;
        }

        ParseQuery<ParseObject> query = getPurchasesQuery();
        query.setSkip(skip);
        query.whereEqualTo(Purchase.GROUP, group);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onPurchasesQueryFailed(ParseErrorHandler.getErrorMessage(context, e));
                    }
                    return;
                }

                // Add the latest results for this query to the cache
                ParseObject.pinAllInBackground(Purchase.PIN_LABEL + group.getObjectId(), parseObjects, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            listener.onPurchasesQueried(parseObjects);
                        }
                    }
                });
            }
        });
    }

    public static void queryPurchase(final Context context, String purchasedId,
                                     final boolean isNew) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Purchase.CLASS);
        query.include(Purchase.ITEMS);
        query.include(Purchase.USERS_INVOLVED);
        query.include(Purchase.BUYER);
        query.getInBackground(purchasedId, new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    return;
                }

                final String groupId = ((Purchase) parseObject).getGroup().getObjectId();
                final String pinLabel = Purchase.PIN_LABEL + groupId;

                if (isNew) {
                    pinPurchase(parseObject, pinLabel, context);
                } else {
                    // although we only update an existing purchase, we need to unpin and repin it
                    // because the items have changed
                    parseObject.unpinInBackground(pinLabel, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                return;
                            }

                            pinPurchase(parseObject, pinLabel, context);
                        }
                    });
                }
            }
        });
    }

    private static void pinPurchase(ParseObject parseObject, String pinLabel, final Context context) {
        parseObject.pinInBackground(pinLabel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    notifyPurchaseChange(context, null);
                }
            }
        });
    }

    public static void queryUsers(final Context context, final UserPinListener listener) {
        ParseQuery<ParseUser> query = User.getQuery();
        query.whereContainedIn(User.GROUPS, getCurrentUserGroups());
        query.whereEqualTo(User.IS_DELETED, false);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> userList, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onUsersPinFailed(ParseErrorHandler.getErrorMessage(context, e));
                    }
                    return;
                }

                // Release any objects previously pinned for this query.
                ParseObject.unpinAllInBackground(User.PIN_LABEL, new DeleteCallback() {
                    public void done(ParseException e) {
                        if (e != null) {
                            return;
                        }

                        // Add the latest results for this query to the cache.
                        ParseObject.pinAllInBackground(User.PIN_LABEL, userList, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    notifyUserChange(context, listener);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public static void updateCurrentUser(final Context context, final UserPinListener listener) {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        currentUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onUsersPinFailed(ParseErrorHandler.getErrorMessage(context, e));
                    }
                    return;
                }

                if (listener != null) {
                    listener.onUsersPinned();
                }
            }
        });
    }

    public static void queryCompensations(final Context context,
                                          final CompensationPinListener listener) {
        List<ParseObject> groups = getCurrentUserGroups();
        if (groups.isEmpty()) {
            return;
        }

        queryCompensationsUnpaid(context, listener, groups);
        for (ParseObject group : groups) {
            queryCompensationsPaid(context, listener, group);
        }
    }

    private static void queryCompensationsUnpaid(final Context context,
                                                 final CompensationPinListener listener,
                                                 List<ParseObject> groups) {
        ParseQuery<ParseObject> query = getCompensationsQuery();
        query.whereContainedIn(Compensation.GROUP, groups);
        query.whereEqualTo(Compensation.IS_PAID, false);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCompensationsPinFailed(ParseErrorHandler.getErrorMessage(context, e));
                    }
                    return;
                }

                final String pinLabel = Compensation.PIN_LABEL_UNPAID;

                ParseObject.unpinAllInBackground(pinLabel, new DeleteCallback() {
                    public void done(ParseException e) {
                        if (e != null) {
                            return;
                        }

                        ParseObject.pinAllInBackground(pinLabel, parseObjects, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    notifyCompensationChange(context, listener, false);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private static void queryCompensationsPaid(final Context context,
                                               final CompensationPinListener listener,
                                               final ParseObject group) {
        ParseQuery<ParseObject> query = getCompensationsQuery();
        query.whereEqualTo(Compensation.GROUP, group);
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.setLimit(QUERY_ITEMS_PER_PAGE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCompensationsPinFailed(ParseErrorHandler.getErrorMessage(context, e));
                    }
                    return;
                }

                final String groupId = group.getObjectId();
                final String pinLabel = Compensation.PIN_LABEL_PAID + groupId;

                ParseObject.unpinAllInBackground(pinLabel, new DeleteCallback() {
                    public void done(ParseException e) {
                        if (e != null) {
                            return;
                        }

                        ParseObject.pinAllInBackground(pinLabel, parseObjects, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    notifyCompensationChange(context, listener, true);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private static ParseQuery<ParseObject> getCompensationsQuery() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        query.orderByDescending(DATE_CREATED);
        return query;
    }

    public static void queryCompensationsPaidMore(final Context context,
                                                  final CompensationQueryListener listener,
                                                  int skip) {
        final Group group = getCurrentGroup();
        if (group == null) {
            return;
        }

        ParseQuery<ParseObject> query = getCompensationsQuery();
        query.setSkip(skip);
        query.whereEqualTo(Compensation.GROUP, group);
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCompensationsQueryFailed(ParseErrorHandler.getErrorMessage(context, e));
                    }
                    return;
                }

                // Add the latest results for this query to the cache.
                ParseObject.pinAllInBackground(Compensation.PIN_LABEL_PAID + group.getObjectId(),
                        parseObjects, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    listener.onCompensationsQueried(parseObjects);
                                }
                            }
                        });
            }
        });
    }

    public static void queryCompensation(final Context context, String compensationId,
                                         final boolean isNew) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        query.getInBackground(compensationId, new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    return;
                }

                if (isNew) {
                    Compensation compensation = (Compensation) parseObject;
                    String groupId = compensation.getGroup().getObjectId();
                    boolean isPaid = compensation.isPaid();
                    String pinLabel;
                    if (isPaid) {
                        pinLabel = Compensation.PIN_LABEL_PAID + groupId;
                    } else {
                        pinLabel = Compensation.PIN_LABEL_UNPAID;
                    }

                    pinCompensation(parseObject, pinLabel, context, isPaid);
                } else {
                    // compensation is now done for sure, hence change label to PAID
                    parseObject.unpinInBackground(Compensation.PIN_LABEL_UNPAID, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                return;
                            }

                            Compensation compensation = (Compensation) parseObject;
                            String groupId = compensation.getGroup().getObjectId();
                            String pinLabel = Compensation.PIN_LABEL_PAID + groupId;

                            pinCompensation(parseObject, pinLabel, context, true);
                        }
                    });
                }
            }
        });
    }

    private static void pinCompensation(ParseObject compensations, String pinLabel,
                                        final Context context, final boolean isPaid) {
        compensations.pinInBackground(pinLabel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    notifyCompensationChange(context, null, isPaid);
                }
            }
        });
    }

    public static void queryCompensationsForGroups(final Context context, List<ParseObject> groups,
                                                   final CompensationQueryListener listener) {
        ParseQuery<ParseObject> payerQuery = ParseQuery.getQuery(Compensation.CLASS);
        payerQuery.whereEqualTo(Compensation.PAYER, ParseUser.getCurrentUser());

        ParseQuery<ParseObject> beneficiaryQuery = ParseQuery.getQuery(Compensation.CLASS);
        beneficiaryQuery.whereEqualTo(Compensation.BENEFICIARY, ParseUser.getCurrentUser());

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(payerQuery);
        queries.add(beneficiaryQuery);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.whereContainedIn(Compensation.GROUP, groups);
        query.whereEqualTo(Compensation.IS_PAID, false);
        query.orderByDescending(DATE_CREATED);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCompensationsQueryFailed(ParseErrorHandler.getErrorMessage(context, e));
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCompensationsQueried(parseObjects);
                }
            }
        });
    }

    public static void queryGroup(final Context context, String groupId,
                                  final GroupQueryListener listener) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Group.CLASS);
        query.getInBackground(groupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onGroupsQueryFailed(ParseErrorHandler.getErrorMessage(context, e));
                    }
                    return;
                }

                listener.onGroupQueried(parseObject);
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

    private static List<ParseObject> getCurrentUserGroups() {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser == null) {
            return Collections.emptyList();
        }

        return currentUser.getGroups();
    }

    public static void notifyPurchaseChange(Context context, PurchasePinListener listener) {
        if (listener != null) {
            listener.onPurchasesPinned();
        } else {
            sendLocalBroadcast(context, DATA_TYPE_PURCHASE);
        }
    }

    public static void notifyUserChange(Context context, UserPinListener listener) {
        if (listener != null) {
            listener.onUsersPinned();
        } else {
            sendLocalBroadcast(context, DATA_TYPE_USER);
        }
    }

    public static void notifyCompensationChange(Context context, CompensationPinListener listener,
                                                boolean isPaid) {
        if (listener != null) {
            listener.onCompensationsPinned(isPaid);
        } else {
            sendLocalBroadcastCompensation(context, isPaid);
        }
    }

    private static void sendLocalBroadcast(Context context, @DataType int dataType) {
        Intent intent = new Intent(INTENT_FILTER_DATA_NEW);
        intent.putExtra(INTENT_DATA_TYPE, dataType);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void sendLocalBroadcastCompensation(Context context, boolean isPaid) {
        Intent intent = new Intent(INTENT_FILTER_DATA_NEW);
        intent.putExtra(INTENT_DATA_TYPE, DATA_TYPE_COMPENSATION);
        intent.putExtra(INTENT_COMPENSATION_PAID, isPaid);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public interface PurchasePinListener {
        void onPurchasePinFailed(String errorMessage);
        void onPurchasesPinned();
    }

    public interface PurchaseQueryListener {
        void onPurchasesQueryFailed(String errorMessage);
        void onPurchasesQueried(List<ParseObject> purchases);
    }

    public interface CompensationPinListener {
        void onCompensationsPinFailed(String errorMessage);
        void onCompensationsPinned(boolean isPaid);
    }

    public interface CompensationQueryListener {
        void onCompensationsQueryFailed(String errorMessage);
        void onCompensationsQueried(List<ParseObject> compensations);
    }

    public interface UserPinListener {
        void onUsersPinFailed(String errorMessage);
        void onUsersPinned();
    }

    public interface GroupQueryListener {
        void onGroupsQueryFailed(String errorMessage);
        void onGroupQueried(ParseObject parseObject);
    }
}