package ch.giantific.qwittig.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ParseQueryService extends IntentService {
    public static final String INTENT_FILTER_DATA_NEW = "ch.giantific.qwittig.push.DATA_NEW";
    public static final String INTENT_DATA_TYPE = "intent_data_type";
    public static final String INTENT_COMPENSATION_PAID = "intent_compensation_paid";

    @IntDef({DATA_TYPE_ALL, DATA_TYPE_PURCHASE, DATA_TYPE_USER, DATA_TYPE_COMPENSATION,
            DATA_TYPE_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DataType {}
    public static final int DATA_TYPE_ALL = 1;
    public static final int DATA_TYPE_PURCHASE = 2;
    public static final int DATA_TYPE_USER = 3;
    public static final int DATA_TYPE_COMPENSATION = 4;
    public static final int DATA_TYPE_GROUP = 5;

    private static final String SERVICE_NAME = "ParseQueryService";

    private static final String ACTION_UNPIN_OBJECT = "ch.giantific.qwittig.services.action.UNPIN_OBJECT";
    private static final String ACTION_QUERY_OBJECT = "ch.giantific.qwittig.services.action.QUERY_OBJECT";
    private static final String ACTION_QUERY_USERS = "ch.giantific.qwittig.services.action.QUERY_USERS";
    private static final String ACTION_QUERY_ALL = "ch.giantific.qwittig.services.action.QUERY_ALL";

    private static final String EXTRA_OBJECT_CLASS = "ch.giantific.qwittig.services.extra.OBJECT_CLASS";
    private static final String EXTRA_OBJECT_ID = "ch.giantific.qwittig.services.extra.OBJECT_ID";
    private static final String EXTRA_OBJECT_IS_NEW = "ch.giantific.qwittig.services.extra.OBJECT_IS_NEW";
    private static final String EXTRA_OBJECT_GROUP_ID = "ch.giantific.qwittig.services.extra.GROUP_ID";

    public ParseQueryService() {
        super(SERVICE_NAME);
    }

    /**
     * Starts this service to unpin a specific ParseObject
     *
     * @see IntentService
     */
    public static void startUnpinObject(Context context, String className, String objectId) {
        startUnpinObject(context, className, objectId, "");
    }

    public static void startUnpinObject(Context context, String className, String objectId,
                                        String groupId) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_UNPIN_OBJECT);
        intent.putExtra(EXTRA_OBJECT_CLASS, className);
        intent.putExtra(EXTRA_OBJECT_ID, objectId);
        intent.putExtra(EXTRA_OBJECT_GROUP_ID, groupId);
        context.startService(intent);
    }

    /**
     * Starts this service to query a specific ParseObject
     *
     * @see IntentService
     */
    public static void startQueryObject(Context context, String className, String objectId,
                                        boolean isNew) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_QUERY_OBJECT);
        intent.putExtra(EXTRA_OBJECT_CLASS, className);
        intent.putExtra(EXTRA_OBJECT_ID, objectId);
        intent.putExtra(EXTRA_OBJECT_IS_NEW, isNew);
        context.startService(intent);
    }

    /**
     * Starts this service to perform to query all users
     *
     * @see IntentService
     */
    public static void startQueryUsers(Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_QUERY_USERS);
        context.startService(intent);
    }

    /**
     * Starts this service to perform to query all users, purchases and compensations
     *
     * @see IntentService
     */
    public static void startQueryAll(Context context) {
        Intent intent = new Intent(context, ParseQueryService.class);
        intent.setAction(ACTION_QUERY_ALL);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();
        switch (action) {
            case ACTION_UNPIN_OBJECT: {
                final String className = intent.getStringExtra(EXTRA_OBJECT_CLASS);
                final String objectId = intent.getStringExtra(EXTRA_OBJECT_ID);
                final String groupId = intent.getStringExtra(EXTRA_OBJECT_GROUP_ID);
                try {
                    unpinObject(className, objectId, groupId);
                } catch (ParseException e) {
                    return;
                }

                break;
            }
            case ACTION_QUERY_OBJECT: {
                final String className = intent.getStringExtra(EXTRA_OBJECT_CLASS);
                final String objectId = intent.getStringExtra(EXTRA_OBJECT_ID);
                final boolean isNew = intent.getBooleanExtra(EXTRA_OBJECT_IS_NEW, false);
                try {
                    queryObject(className, objectId, isNew);
                } catch (ParseException e) {
                    return;
                }

                break;
            }
            case ACTION_QUERY_USERS: {
                try {
                    queryUsers();
                } catch (ParseException ignored) {
                    return;
                }

                break;
            }
            case ACTION_QUERY_ALL: {
                try {
                    queryUsers();
                    queryPurchases();
                    queryCompensations();
                } catch (ParseException e) {
                    return;
                }

                break;
            }
        }
    }

    private void unpinObject(String className, String objectId, String groupId) throws ParseException {
        switch (className) {
            case Purchase.CLASS: {
                ParseObject purchase = ParseObject.createWithoutData(Purchase.CLASS, objectId);
                purchase.unpin(Purchase.PIN_LABEL + groupId);
                sendLocalBroadcast(DATA_TYPE_PURCHASE);
                break;
            }
            case Compensation.CLASS: {
                ParseObject compensations = ParseObject.createWithoutData(Compensation.CLASS, objectId);
                compensations.unpin(Compensation.PIN_LABEL_UNPAID);
                sendLocalBroadcastCompensation(false);
                break;
            }
        }
    }

    private void queryObject(String className, String objectId, boolean isNew) throws ParseException {
        switch (className) {
            case Purchase.CLASS:
                queryPurchase(objectId, isNew);
                sendLocalBroadcast(DATA_TYPE_PURCHASE);
                break;
            case Compensation.CLASS:
                boolean isPaid = queryCompensation(objectId, isNew);
                sendLocalBroadcastCompensation(isPaid);
                break;
            case Group.CLASS:
                queryGroup(objectId);
                break;
        }
    }

    private void queryPurchase(String objectId, boolean isNew) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Purchase.CLASS);
        query.include(Purchase.ITEMS);
        query.include(Purchase.USERS_INVOLVED);
        query.include(Purchase.BUYER);
        Purchase purchase = (Purchase) query.get(objectId);

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
    }

    public boolean queryCompensation(String compensationId, boolean isNew) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Compensation.CLASS);
        Compensation compensation = (Compensation) query.get(compensationId);

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
    }

    private void queryGroup(String groupId) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Group.CLASS);
        query.get(groupId);
    }

    private void queryPurchases() throws ParseException {
        List<ParseObject> groups = ParseUtils.getCurrentUserGroups();
        if (groups.isEmpty()) {
            return;
        }

        for (final ParseObject group : groups) {
            ParseQuery<ParseObject> query = OnlineQuery.getPurchasesQuery();
            query.whereEqualTo(Purchase.GROUP, group);
            List<ParseObject> purchases = query.find();

            final String groupId = group.getObjectId();
            final String label = Purchase.PIN_LABEL + groupId;

            ParseObject.unpinAll(label);
            ParseObject.pinAll(label, purchases);
        }

        sendLocalBroadcast(DATA_TYPE_PURCHASE);
    }

    private void queryUsers() throws ParseException {
        ParseQuery<ParseUser> query = User.getQuery();
        query.whereContainedIn(User.GROUPS, ParseUtils.getCurrentUserGroups());
        query.whereEqualTo(User.IS_DELETED, false);
        List<ParseUser> users = query.find();

        ParseObject.unpinAll(User.PIN_LABEL);
        ParseObject.pinAll(User.PIN_LABEL, users);

        sendLocalBroadcast(DATA_TYPE_USER);
    }

    private void queryCompensations() throws ParseException {
        List<ParseObject> groups = ParseUtils.getCurrentUserGroups();
        if (groups.isEmpty()) {
            return;
        }

        queryCompensationsUnpaid(groups);
        sendLocalBroadcastCompensation(false);

        for (ParseObject group : groups) {
            queryCompensationsPaid(group);
        }
        sendLocalBroadcastCompensation(true);
    }

    private void queryCompensationsUnpaid(List<ParseObject> groups) throws ParseException {
        ParseQuery<ParseObject> query = OnlineQuery.getCompensationsQuery();
        query.whereContainedIn(Compensation.GROUP, groups);
        query.whereEqualTo(Compensation.IS_PAID, false);
        List<ParseObject> compensationsUnpaid = query.find();

        ParseObject.unpinAll(Compensation.PIN_LABEL_UNPAID);
        ParseObject.pinAll(Compensation.PIN_LABEL_UNPAID, compensationsUnpaid);
    }

    private void queryCompensationsPaid(final ParseObject group) throws ParseException {
        ParseQuery<ParseObject> query = OnlineQuery.getCompensationsQuery();
        query.whereEqualTo(Compensation.GROUP, group);
        query.whereEqualTo(Compensation.IS_PAID, true);
        query.setLimit(OnlineQuery.QUERY_ITEMS_PER_PAGE);
        List<ParseObject> compensationsPaid = query.find();

        final String groupId = group.getObjectId();
        final String pinLabel = Compensation.PIN_LABEL_PAID + groupId;
        ParseObject.unpinAll(pinLabel);
        ParseObject.pinAll(pinLabel, compensationsPaid);
    }

    private void sendLocalBroadcast(@DataType int dataType) {
        Intent intent = new Intent(INTENT_FILTER_DATA_NEW);
        intent.putExtra(INTENT_DATA_TYPE, dataType);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendLocalBroadcastCompensation(boolean isPaid) {
        Intent intent = new Intent(INTENT_FILTER_DATA_NEW);
        intent.putExtra(INTENT_DATA_TYPE, DATA_TYPE_COMPENSATION);
        intent.putExtra(INTENT_COMPENSATION_PAID, isPaid);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
