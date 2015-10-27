/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.Task;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Provides an abstract base class for helper fragments whose task it is to query data from the
 * online Parse.com database.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public abstract class BaseQueryHelper extends BaseHelper {

    private static final String LOG_TAG = BaseQueryHelper.class.getSimpleName();
    Group mCurrentGroup;
    List<ParseObject> mCurrentUserGroups;
    int mTotalNumberOfQueries;
    private int mQueryCount;

    public BaseQueryHelper() {
        // empty default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCurrentGroups();
    }

    private void setCurrentGroups() {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser != null) {
            mCurrentGroup = currentUser.getCurrentGroup();
            mCurrentUserGroups = currentUser.getGroups();
        }
    }

    final void calculateBalances() {
        Map<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground(CloudCode.CALCULATE_BALANCE, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                onBalancesCalculated();
            }
        });
    }

    @CallSuper
    void onBalancesCalculated() {
        // empty default implementation
    }

    final void queryUsers() {
        ParseQuery<ParseUser> query = User.getQuery();
        query.whereContainedIn(User.GROUPS, mCurrentUserGroups);
        query.whereEqualTo(User.IS_DELETED, false);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(@NonNull final List<ParseUser> userList, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                // Release any objects previously pinned for this query.
                ParseObject.unpinAllInBackground(User.PIN_LABEL, new DeleteCallback() {
                    public void done(@Nullable ParseException e) {
                        if (e != null) {
                            onParseError(e);
                            return;
                        }

                        // Add the latest results for this query to the cache.
                        ParseObject.pinAllInBackground(User.PIN_LABEL, userList, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e == null) {
                                    onUsersPinned();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @CallSuper
    void onUsersPinned() {
        // empty default implementation
    }

    final void queryPurchases() {
        for (final ParseObject group : mCurrentUserGroups) {
            ParseQuery<ParseObject> query = OnlineQuery.getPurchasesQuery();
            query.whereEqualTo(Purchase.GROUP, group);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                    if (e != null) {
                        onParseError(e);
                        return;
                    }

                    final String groupId = group.getObjectId();
                    final String label = Purchase.PIN_LABEL + groupId;

                    // Release any objects previously pinned for the purchase pin label
                    ParseObject.unpinAllInBackground(label, new DeleteCallback() {
                        @Override
                        public void done(@Nullable ParseException e) {
                            if (e != null) {
                                onParseError(e);
                                return;
                            }

                            // Add the latest results for this query to the cache
                            ParseObject.pinAllInBackground(label, parseObjects, new SaveCallback() {
                                @Override
                                public void done(@Nullable ParseException e) {
                                    if (e == null) {
                                        onPurchasesPinned(groupId);
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    @CallSuper
    void onPurchasesPinned(String groupId) {
        // empty default implementation
    }

    final void queryCompensations() {
        queryCompensationsUnpaid(mCurrentUserGroups);

        for (ParseObject group : mCurrentUserGroups) {
            queryCompensationsPaid(group);
        }
    }

    final void queryCompensationsUnpaid(@NonNull List<ParseObject> groups) {
        ParseQuery<ParseObject> query = OnlineQuery.getCompensationsQuery();
        query.whereContainedIn(Compensation.GROUP, groups);
        query.whereEqualTo(Compensation.IS_PAID, false);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                ParseObject.unpinAllInBackground(Compensation.PIN_LABEL_UNPAID, new DeleteCallback() {
                    public void done(@Nullable ParseException e) {
                        if (e != null) {
                            onParseError(e);
                            return;
                        }

                        ParseObject.pinAllInBackground(Compensation.PIN_LABEL_UNPAID, parseObjects, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e == null) {
                                    onCompensationsUnpaidPinned();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @CallSuper
    void onCompensationsUnpaidPinned() {
        // empty default implementation
    }

    final void queryCompensationsPaid(@NonNull final ParseObject group) {
        ParseQuery<ParseObject> query = OnlineQuery.getCompensationsQuery();
        query.whereEqualTo(Compensation.GROUP, group);
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.setLimit(OnlineQuery.QUERY_ITEMS_PER_PAGE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                final String groupId = group.getObjectId();
                final String pinLabel = Compensation.PIN_LABEL_PAID + groupId;

                ParseObject.unpinAllInBackground(pinLabel, new DeleteCallback() {
                    public void done(@Nullable ParseException e) {
                        if (e != null) {
                            return;
                        }

                        ParseObject.pinAllInBackground(pinLabel, parseObjects, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e == null) {
                                    onCompensationsPaidPinned(groupId);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @CallSuper
    void onCompensationsPaidPinned(String groupId) {
        // empty default implementation
    }

    final void queryTasks() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Task.CLASS);
        query.whereContainedIn(Task.GROUP, mCurrentUserGroups);
        query.include(Task.USERS_INVOLVED);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(@NonNull final List<ParseObject> parseObjects, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                // Release any objects previously pinned for this query.
                ParseObject.unpinAllInBackground(Task.PIN_LABEL, new DeleteCallback() {
                    public void done(@Nullable ParseException e) {
                        if (e != null) {
                            onParseError(e);
                            return;
                        }

                        // Add the latest results for this query to the cache.
                        ParseObject.pinAllInBackground(Task.PIN_LABEL, parseObjects, new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e == null) {
                                    onTasksPinned();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @CallSuper
    void onTasksPinned() {
        // empty default implementation

    }

    final boolean checkQueryCount() {
        mQueryCount++;

        if (mQueryCount == mTotalNumberOfQueries) {
            finish();
            return true;
        }

        return false;
    }

    protected abstract void onParseError(ParseException e);

    protected abstract void finish();
}
