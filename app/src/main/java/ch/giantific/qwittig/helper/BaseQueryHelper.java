package ch.giantific.qwittig.helper;

import android.os.Bundle;
import android.support.annotation.CallSuper;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Created by fabio on 10.12.14.
 */
public abstract class BaseQueryHelper extends BaseHelper {

    private static final String LOG_TAG = BaseQueryHelper.class.getSimpleName();
    Group mCurrentGroup;

    public BaseQueryHelper() {
        // empty default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCurrentGroup();
    }

    private void setCurrentGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser != null) {
            mCurrentGroup = currentUser.getCurrentGroup();
        }
    }

    final void queryUsers() {
        ParseQuery<ParseUser> query = User.getQuery();
        query.whereContainedIn(User.GROUPS, ParseUtils.getCurrentUserGroups());
        query.whereEqualTo(User.IS_DELETED, false);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> userList, ParseException e) {
                if (e != null) {
                    onParseError(e);
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
        List<ParseObject> groups = ParseUtils.getCurrentUserGroups();
        if (groups.isEmpty()) {
            return;
        }

        for (final ParseObject group : groups) {
            ParseQuery<ParseObject> query = OnlineQuery.getPurchasesQuery();
            query.whereEqualTo(Purchase.GROUP, group);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(final List<ParseObject> parseObjects, ParseException e) {
                    if (e != null) {
                        onParseError(e);
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
        List<ParseObject> groups = ParseUtils.getCurrentUserGroups();
        if (groups.isEmpty()) {
            return;
        }

        queryCompensationsUnpaid(groups);
        for (ParseObject group : groups) {
            queryCompensationsPaid(group);
        }
    }

    private void queryCompensationsUnpaid(List<ParseObject> groups) {
        ParseQuery<ParseObject> query = OnlineQuery.getCompensationsQuery();
        query.whereContainedIn(Compensation.GROUP, groups);
        query.whereEqualTo(Compensation.IS_PAID, false);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                ParseObject.unpinAllInBackground(Compensation.PIN_LABEL_UNPAID, new DeleteCallback() {
                    public void done(ParseException e) {
                        if (e != null) {
                            return;
                        }

                        ParseObject.pinAllInBackground(Compensation.PIN_LABEL_UNPAID, parseObjects, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
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

    private void queryCompensationsPaid(final ParseObject group) {
        ParseQuery<ParseObject> query = OnlineQuery.getCompensationsQuery();
        query.whereEqualTo(Compensation.GROUP, group);
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.setLimit(OnlineQuery.QUERY_ITEMS_PER_PAGE);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> parseObjects, ParseException e) {
                if (e != null) {
                    onParseError(e);
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

    protected abstract void onParseError(ParseException e);
}
