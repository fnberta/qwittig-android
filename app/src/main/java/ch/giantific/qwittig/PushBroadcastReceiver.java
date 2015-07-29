package ch.giantific.qwittig;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StringDef;
import android.text.TextUtils;
import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Compensation;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.CompensationsActivity;
import ch.giantific.qwittig.ui.PurchaseDetailsActivity;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 07.12.14.
 */
public class PushBroadcastReceiver extends ParsePushBroadcastReceiver {

    public static final String NOTIFICATION_TYPE = "type";
    public static final String PUSH_PARAM_PURCHASE = "purchase";
    public static final String PUSH_PARAM_COMPENSATION = "compensation";
    public static final String PUSH_PARAM_GROUP = "group";
    public static final String PUSH_PARAM_PAYER = "payer";
    public static final String PUSH_PARAM_BENEFICIARY = "beneficiary";
    public static final String PUSH_PARAM_TITLE = "title";
    public static final String PUSH_PARAM_ALERT = "alert";
    public static final String PUSH_PARAM_USER = "user";
    public static final String PUSH_PARAM_BUYER = "buyer";
    public static final String PUSH_PARAM_AMOUNT = "amount";
    public static final String PUSH_PARAM_STORE = "store";
    public static final String PUSH_PARAM_TOTAL_PRICE = "totalPrice";
    public static final String PUSH_PARAM_USERS_INVOLVED = "usersInvolved";
    public static final String PUSH_PARAM_INITIATOR = "initiator";
    public static final String PUSH_PARAM_GROUP_NAME = "groupName";
    public static final String PUSH_PARAM_CURRENCY_CODE = "currencyCode";

    public static final String INTENT_ACTION_INVITATION = "intent_action_invitation";
    public static final int ACTION_INVITATION_ACCEPTED = 1;
    public static final int ACTION_INVITATION_DISCARDED = 2;

    @StringDef({TYPE_PURCHASE_NEW, TYPE_PURCHASE_EDIT, TYPE_PURCHASE_DELETE,
            TYPE_SETTLEMENT_NEW, TYPE_COMPENSATION_NEW_PAID, TYPE_COMPENSATION_NEW_UNPAID,
            TYPE_COMPENSATION_REMIND_USER, TYPE_COMPENSATION_REMIND_USER_HAS_PAID,
            TYPE_COMPENSATION_EXISTING_NOT_NOW, TYPE_COMPENSATION_EXISTING_PAID, TYPE_USER_INVITED,
            TYPE_USER_JOINED, TYPE_USER_LEFT, TYPE_USER_DELETED, TYPE_GROUP_USERS_INVITED_CHANGED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NotificationType {}
    public static final String TYPE_USER_INVITED = "userInvited";
    private static final String TYPE_PURCHASE_NEW = "purchaseNew";
    private static final String TYPE_PURCHASE_EDIT = "purchaseEdit";
    private static final String TYPE_PURCHASE_DELETE = "purchaseDelete";
    private static final String TYPE_SETTLEMENT_NEW = "settlementNew";
    private static final String TYPE_COMPENSATION_NEW_PAID = "compensationNewPaid";
    private static final String TYPE_COMPENSATION_NEW_UNPAID = "compensationNewUnpaid";
    private static final String TYPE_COMPENSATION_EXISTING_PAID = "compensationExistingPaid";
    private static final String TYPE_COMPENSATION_EXISTING_NOT_NOW = "compensationExistingNotNow";
    private static final String TYPE_COMPENSATION_REMIND_USER = "compensationRemindUser";
    private static final String TYPE_COMPENSATION_REMIND_USER_HAS_PAID = "compensationRemindUserHasPaid";
    private static final String TYPE_COMPENSATION_AMOUNT_CHANGED = "compensationAmountChanged";
    private static final String TYPE_USER_JOINED = "userJoined";
    private static final String TYPE_USER_LEFT = "userLeft";
    private static final String TYPE_USER_DELETED = "userDeleted";
    private static final String TYPE_GROUP_NAME_CHANGED = "groupNameChanged";
    private static final String TYPE_GROUP_USERS_INVITED_CHANGED = "groupUsersInvitedChanged";

    private static final String ACTION_PUSH_BUTTON_ACCEPT = "ch.giantific.qwittig.push.intent.ACCEPT";
    private static final String ACTION_PUSH_BUTTON_DISCARD = "ch.giantific.qwittig.push.intent.DISCARD";
    private static final String NOTIFICATION_ID = "notification_id";
    private static final int NEW_PURCHASE_NOTIFICATION_ID = 1;
    private static final String STORED_PURCHASE_NOTIFICATIONS = "stored_purchase_notifications_";
    private static final int MAX_LINES_INBOX_STYLE = 7;

    private static final String LOG_TAG = PushBroadcastReceiver.class.getSimpleName();

    private SharedPreferences mSharedPreferences;
    private NotificationManager mNotificationManager;
    private Set<String> mPurchaseNotifications;

    @Override
    public void onReceive(Context context, Intent intent) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String action = intent.getAction();
        switch (action) {
            case ACTION_PUSH_BUTTON_ACCEPT: {
                JSONObject jsonExtras = null;
                try {
                    jsonExtras = getData(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (jsonExtras == null) {
                    super.onReceive(context, intent);
                    return;
                }

                String type = jsonExtras.optString(NOTIFICATION_TYPE);
                switch (type) {
                    case TYPE_COMPENSATION_REMIND_USER_HAS_PAID: {
                        Compensation compensation = getCompensation(jsonExtras);
                        compensation.setPaid(true);
                        compensation.saveEventually();

                        cancelNotification(intent);
                        break;
                    }
                    case TYPE_USER_INVITED: {
                        intent.putExtra(INTENT_ACTION_INVITATION, ACTION_INVITATION_ACCEPTED);
                        onPushOpen(context, intent);

                        cancelNotification(intent);
                        closeShade(context);
                        break;
                    }
                }
                break;
            }
            case ACTION_PUSH_BUTTON_DISCARD: {
                JSONObject jsonExtras = null;
                try {
                    jsonExtras = getData(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (jsonExtras == null) {
                    super.onReceive(context, intent);
                    return;
                }

                String type = jsonExtras.optString(NOTIFICATION_TYPE);
                switch (type) {
                    case TYPE_COMPENSATION_REMIND_USER:
                        Compensation compensation = getCompensation(jsonExtras);
                        compensation.deleteEventually();

                        cancelNotification(intent);
                        break;
                    case TYPE_USER_INVITED:
                        intent.putExtra(INTENT_ACTION_INVITATION, ACTION_INVITATION_DISCARDED);
                        onPushOpen(context, intent);

                        cancelNotification(intent);
                        closeShade(context);
                        break;
                }
                break;
            }
            default:
                super.onReceive(context, intent);
        }
    }

    private Compensation getCompensation(JSONObject jsonExtras) {
        String compensationId = jsonExtras.optString(PUSH_PARAM_COMPENSATION);
        return (Compensation) ParseObject.createWithoutData(Compensation.CLASS, compensationId);
    }

    private void cancelNotification(Intent intent) {
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        mNotificationManager.cancel(notificationId);
    }

    private void closeShade(Context context) {
        Intent closeShade = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeShade);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Log.e(LOG_TAG, "onPushReceive");

        JSONObject jsonExtras = null;
        try {
            jsonExtras = getData(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // if there is no data or no user is logged in, return immediately
        if (jsonExtras == null || ParseUser.getCurrentUser() == null) {
            Log.e(LOG_TAG, "jsonExtras is null " + String.valueOf(jsonExtras == null));
            Log.e(LOG_TAG, "currentUser is null " + String.valueOf(ParseUser.getCurrentUser() == null));
            return;
        }

        int notificationId = (int) System.currentTimeMillis();
        String notificationTag = null;
        String type = getNotificationType(intent);
        switch (type) {
            case TYPE_PURCHASE_NEW: {
                // set notification id and tag
                notificationId = NEW_PURCHASE_NOTIFICATION_ID;
                notificationTag = jsonExtras.optString(PUSH_PARAM_GROUP);

                // update balance for all users
                OnlineQuery.queryUsers(context, null);

                // query purchase only for users in purchase's usersInvolved
                if (!userIsInUsersInvolved(jsonExtras)) {
                    return;
                }
                String purchaseId = jsonExtras.optString(PUSH_PARAM_PURCHASE);
                OnlineQuery.queryPurchase(context, purchaseId, true);

                // don't show notification for buyer
                String buyerId = jsonExtras.optString(PUSH_PARAM_BUYER);
                if (buyerId.equals(ParseUser.getCurrentUser().getObjectId())) {
                    return;
                }

                // store text in LinkedHashSet for combined notifications
                String store = jsonExtras.optString(PUSH_PARAM_STORE);
                String currencyCode = jsonExtras.optString(PUSH_PARAM_CURRENCY_CODE);
                String totalAmountFormatted = getPurchaseTotalAmount(jsonExtras, currencyCode);
                mPurchaseNotifications = mSharedPreferences.getStringSet(
                        STORED_PURCHASE_NOTIFICATIONS + notificationTag, new LinkedHashSet<String>());
                mPurchaseNotifications.add(store + ": " + totalAmountFormatted);

                // save set in sharedPreferences
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putStringSet(STORED_PURCHASE_NOTIFICATIONS + notificationTag, mPurchaseNotifications);
                editor.apply();
                break;
            }
            case TYPE_PURCHASE_EDIT: {
                // update balance for all users
                OnlineQuery.queryUsers(context, null);

                // only update purchase for users in purchase's usersInvolved
                if (!userIsInUsersInvolved(jsonExtras)) {
                    return;
                }
                String purchaseId = jsonExtras.optString(PUSH_PARAM_PURCHASE);
                OnlineQuery.queryPurchase(context, purchaseId, false);
                break;
            }
            case TYPE_PURCHASE_DELETE: {
                // update balance for all users
                OnlineQuery.queryUsers(context, null);

                // only unpin local purchase for users in purchase's usersInvolved
                if (!userIsInUsersInvolved(jsonExtras)) {
                    return;
                }
                String purchaseId = jsonExtras.optString(PUSH_PARAM_PURCHASE);
                String groupId = jsonExtras.optString(PUSH_PARAM_GROUP);
                LocalQuery.unpinLocalPurchase(context, purchaseId, groupId);
                break;
            }
            case TYPE_SETTLEMENT_NEW: {
                String initiatorId = jsonExtras.optString(PUSH_PARAM_INITIATOR);
                if (initiatorId.equals(ParseUser.getCurrentUser().getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_COMPENSATION_NEW_UNPAID: {
                // query compensation for all users
                queryCompensation(context, jsonExtras, true);
                break;
            }
            case TYPE_COMPENSATION_NEW_PAID: {
                // update balance for all users
                OnlineQuery.queryUsers(context, null);

                // query compensation
                queryCompensation(context, jsonExtras, true);
                break;
            }
            case TYPE_COMPENSATION_EXISTING_PAID: {
                // update balance for all users
                OnlineQuery.queryUsers(context, null);

                // update compensation for all users
                queryCompensation(context, jsonExtras, false);

                // only show notification for payer
                String payerId = jsonExtras.optString(PUSH_PARAM_PAYER);
                if (!payerId.equals(ParseUser.getCurrentUser().getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_COMPENSATION_EXISTING_NOT_NOW: {
                // update balance for all users
                OnlineQuery.queryUsers(context, null);

                // unpin compensation for all users
                String compensationId = jsonExtras.optString(PUSH_PARAM_COMPENSATION);
                LocalQuery.unpinLocalCompensation(context, compensationId);

                // only show notification for beneficiary
                String beneficiaryId = jsonExtras.optString(PUSH_PARAM_BENEFICIARY);
                if (!beneficiaryId.equals(ParseUser.getCurrentUser().getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_COMPENSATION_AMOUNT_CHANGED: {
                // update compensation for all users
                queryCompensation(context, jsonExtras, false);

                // only show notification for beneficiary
                String beneficiaryId = jsonExtras.optString(PUSH_PARAM_BENEFICIARY);
                if (!beneficiaryId.equals(ParseUser.getCurrentUser().getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_USER_DELETED: {
                OnlineQuery.queryPurchases(context, null);
                OnlineQuery.queryUsers(context, null);
                OnlineQuery.queryCompensations(context, null);
                break;
            }
            case TYPE_USER_JOINED:
                // fall through
            case TYPE_USER_LEFT: {
                OnlineQuery.queryUsers(context, null);
                break;
            }
            case TYPE_GROUP_NAME_CHANGED:
                // fall through
            case TYPE_GROUP_USERS_INVITED_CHANGED: {
                String groupId = jsonExtras.optString(PUSH_PARAM_GROUP);
                OnlineQuery.queryGroup(context, groupId, null);
                break;
            }
        }

        // get the notification and display it the data contains a title and/or an alert
        if (!isSilentNotification(type)) {
            intent.putExtra(NOTIFICATION_ID, notificationId);
            Notification notification = getNotification(context, intent);
            mNotificationManager.notify(notificationTag, notificationId, notification);
        }
    }

    private void queryCompensation(Context context, JSONObject jsonExtras, boolean isNew) {
        String compensationId = jsonExtras.optString(PUSH_PARAM_COMPENSATION);
        OnlineQuery.queryCompensation(context, compensationId, isNew);
    }

    private String getPurchaseTotalAmount(JSONObject jsonExtras, String currencyCode) {
        String totalAmount = jsonExtras.optString(PUSH_PARAM_TOTAL_PRICE);
        String totalAmountFormatted = "";
        if (!TextUtils.isEmpty(totalAmount)) {
            totalAmountFormatted = MoneyUtils.formatMoney(Double.parseDouble(totalAmount),
                    currencyCode);
        }
        return totalAmountFormatted;
    }

    private boolean isSilentNotification(String type) {
        return type.equals(TYPE_PURCHASE_EDIT) || type.equals(TYPE_PURCHASE_DELETE) ||
                type.equals(TYPE_COMPENSATION_NEW_PAID) || type.equals(TYPE_COMPENSATION_NEW_UNPAID) ||
                type.equals(TYPE_GROUP_NAME_CHANGED) || type.equals(TYPE_GROUP_USERS_INVITED_CHANGED);
    }

    private String getNotificationType(Intent intent) {
        JSONObject jsonExtras = null;
        try {
            jsonExtras = getData(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonExtras != null) {
            return jsonExtras.optString(NOTIFICATION_TYPE);
        }

        return "";
    }

    private boolean userIsInUsersInvolved(JSONObject jsonExtras) {
        JSONArray usersInvolvedIds = jsonExtras.optJSONArray(PUSH_PARAM_USERS_INVOLVED);
        return usersInvolvedIds.toString().contains(ParseUser.getCurrentUser().getObjectId());
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        JSONObject jsonExtras = null;
        try {
            jsonExtras = getData(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // if we have no data or push has non empty title and/or alert, fall back to super
        // implementation
        if (jsonExtras == null || jsonExtras.has(PUSH_PARAM_TITLE) ||
                jsonExtras.has(PUSH_PARAM_ALERT)) {
            return super.getNotification(context, intent);
        }

        // get extras
        Bundle extras = intent.getExtras();
        String packageName = context.getPackageName();

        // setup intents
        Random random = new Random();
        // openIntent
        Intent contentIntent = new Intent(ACTION_PUSH_OPEN);
        contentIntent.putExtras(extras);
        contentIntent.setPackage(packageName);
        int contentIntentRequestCode = random.nextInt();
        PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // deleteIntent
        Intent deleteIntent = new Intent(ACTION_PUSH_DELETE);
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);
        int deleteIntentRequestCode = random.nextInt();
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // setup notification builder
        Notification.Builder builder = new Notification.Builder(context);
        builder.setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(getSmallIconId(context, intent))
                .setAutoCancel(true)
                .setContentIntent(pContentIntent)
                .setDeleteIntent(pDeleteIntent);

        String user = jsonExtras.optString(PUSH_PARAM_USER);
        String title;
        String alert;
        String type = getNotificationType(intent);
        switch (type) {
            case TYPE_PURCHASE_NEW: {
                String store = jsonExtras.optString(PUSH_PARAM_STORE);
                String currencyCode = jsonExtras.optString(PUSH_PARAM_CURRENCY_CODE);
                String totalAmountFormatted = getPurchaseTotalAmount(jsonExtras, currencyCode);
                title = context.getString(R.string.push_purchase_new_title, user);
                alert = context.getString(R.string.push_purchase_new_alert, store, totalAmountFormatted);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);

                // use inbox style if there are multiple new purchase notifications
                if (mPurchaseNotifications != null && mPurchaseNotifications.size() > 1) {
                    String groupName = jsonExtras.optString(PUSH_PARAM_GROUP_NAME);

                    // create inboxStyle
                    Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
                    for (String purchaseNotification : mPurchaseNotifications) {
                        inboxStyle.addLine(purchaseNotification);
                    }
                    int size = mPurchaseNotifications.size();
                    String summaryText;
                    if (size > MAX_LINES_INBOX_STYLE) {
                        summaryText = "+" + String.valueOf(size - MAX_LINES_INBOX_STYLE) + " " +
                                context.getString(R.string.push_purchase_new_multiple_more);
                    } else {
                        summaryText = groupName;
                    }
                    inboxStyle.setSummaryText(summaryText);

                    // set style, title and text to builder
                    builder.setContentTitle(size + " " + context.getString(R.string.push_purchase_new_multiple_title))
                            .setContentText(groupName)
                            .setStyle(inboxStyle);
                }
                break;
            }
            case TYPE_SETTLEMENT_NEW:
                title =  context.getString(R.string.push_settlement_new_title, user);
                alert = context.getString(R.string.push_settlement_new_alert);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            case TYPE_COMPENSATION_REMIND_USER: {
                String amount = getAmount(jsonExtras);
                title = context.getString(R.string.push_compensation_remind_title);
                alert = context.getString(R.string.push_compensation_remind_alert, user, amount);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);

                // setup action button
                Intent notNowContentIntent = new Intent(ACTION_PUSH_BUTTON_DISCARD);
                notNowContentIntent.setPackage(packageName);
                notNowContentIntent.putExtras(extras);
                int notNowContentIntentRequestCode = random.nextInt();
                PendingIntent pNotNowContentIntent = PendingIntent.getBroadcast(context,
                        notNowContentIntentRequestCode, notNowContentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_clear_black_24dp,
                        context.getString(R.string.action_compensation_not_now), pNotNowContentIntent);
                break;
            }
            case TYPE_COMPENSATION_REMIND_USER_HAS_PAID: {
                String amount = getAmount(jsonExtras);
                title = context.getString(R.string.push_compensation_remind_paid_title, user);
                alert = context.getString(R.string.push_compensation_remind_paid_alert, amount);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);

                // setup action button
                Intent acceptContentIntent = new Intent(ACTION_PUSH_BUTTON_ACCEPT);
                acceptContentIntent.setPackage(packageName);
                acceptContentIntent.putExtras(extras);
                int acceptContentIntentRequestCode = random.nextInt();
                PendingIntent pAcceptContentIntent = PendingIntent.getBroadcast(context,
                        acceptContentIntentRequestCode, acceptContentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_check_black_24dp,
                        context.getString(R.string.push_action_confirm), pAcceptContentIntent);
                break;
            }
            case TYPE_COMPENSATION_EXISTING_PAID: {
                String amount = getAmount(jsonExtras);
                title = context.getString(R.string.push_compensation_payment_done_title);
                alert = context.getString(R.string.push_compensation_payment_done_alert, user, amount);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_COMPENSATION_EXISTING_NOT_NOW: {
                title = context.getString(R.string.push_compensation_not_now_title);
                alert = context.getString(R.string.push_compensation_not_now_alert, user);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_COMPENSATION_AMOUNT_CHANGED: {
                String amount = getAmount(jsonExtras);
                title = context.getString(R.string.push_compensation_amount_changed_title);
                alert = context.getString(R.string.push_compensation_amount_changed_alert, user, amount);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_USER_INVITED: {
                String groupName = jsonExtras.optString(PUSH_PARAM_GROUP_NAME);
                title = context.getString(R.string.push_user_invite_title, groupName);
                alert = context.getString(R.string.push_user_invite_alert, user);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);

                // setup action button
                Intent acceptContentIntent = new Intent(ACTION_PUSH_BUTTON_ACCEPT);
                acceptContentIntent.setPackage(packageName);
                acceptContentIntent.putExtras(extras);
                int acceptContentIntentRequestCode = random.nextInt();
                PendingIntent pAcceptContentIntent = PendingIntent.getBroadcast(context,
                        acceptContentIntentRequestCode, acceptContentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_check_black_24dp,
                        context.getString(R.string.dialog_positive_join), pAcceptContentIntent);

                Intent discardContentIntent = new Intent(ACTION_PUSH_BUTTON_DISCARD);
                discardContentIntent.setPackage(packageName);
                discardContentIntent.putExtras(extras);
                int notNowContentIntentRequestCode = random.nextInt();
                PendingIntent pDiscardContentIntent = PendingIntent.getBroadcast(context,
                        notNowContentIntentRequestCode, discardContentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_clear_black_24dp,
                        context.getString(R.string.dialog_negative_discard), pDiscardContentIntent);
                break;
            }
            case TYPE_USER_DELETED: {
                title = context.getString(R.string.push_user_deleted_title, user);
                alert = context.getString(R.string.push_user_deleted_alert);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_USER_JOINED: {
                String groupName = jsonExtras.optString(PUSH_PARAM_GROUP_NAME);
                title = context.getString(R.string.push_user_joined_title, groupName);
                alert = context.getString(R.string.push_user_joined_alert, user);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_USER_LEFT: {
                String groupName = jsonExtras.optString(PUSH_PARAM_GROUP_NAME);
                title = context.getString(R.string.push_user_left_title, user);
                alert = context.getString(R.string.push_user_left_alert, user, groupName);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            default: {
                return super.getNotification(context, intent);
            }
        }

        // set ticker text for Android <5.0
        builder.setTicker(String.format(Locale.getDefault(), "%s: %s", title, alert));

        // use BigTextStyle if alert is long
        if (alert.length() > SMALL_NOTIFICATION_MAX_CHARACTER_LIMIT) {
            Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle().bigText(alert);
            builder.setStyle(bigTextStyle);
        }

        return builder.build();
    }

    private String getAmount(JSONObject jsonExtras) {
        String currencyCode = jsonExtras.optString(PUSH_PARAM_CURRENCY_CODE);
        double amount = jsonExtras.optDouble(PUSH_PARAM_AMOUNT);
        return MoneyUtils.formatMoney(amount, currencyCode);
    }

    public static JSONObject getData(Intent intent) throws JSONException {
        JSONObject extras;
        if (intent.hasExtra(KEY_PUSH_DATA)) {
            extras = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
        } else {
            extras = new JSONObject("");
        }
        return extras;
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        String type = getNotificationType(intent);
        if (!isSilentNotification(type) && !type.equals(TYPE_USER_INVITED)) {
            JSONObject jsonExtras = null;
            try {
                jsonExtras = getData(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (jsonExtras != null) {
                String groupId = jsonExtras.optString(PUSH_PARAM_GROUP);

                User currentUser = (User) ParseUser.getCurrentUser();
                Group oldGroup = currentUser.getCurrentGroup();
                if (!oldGroup.getObjectId().equals(groupId)) {
                    ParseObject group = ParseObject.createWithoutData(Group.CLASS, groupId);
                    currentUser.setCurrentGroup(group);
                    currentUser.saveEventually();
                }
            }
        }

        super.onPushOpen(context, intent);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        String type = getNotificationType(intent);
        switch (type) {
            case TYPE_PURCHASE_NEW:
                String groupId = "";
                JSONObject jsonExtras = null;
                try {
                    jsonExtras = getData(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (jsonExtras != null) {
                    groupId = jsonExtras.optString(PUSH_PARAM_GROUP);
                }

                mPurchaseNotifications = mSharedPreferences.getStringSet(
                        STORED_PURCHASE_NOTIFICATIONS + groupId, new LinkedHashSet<String>());
                if (mPurchaseNotifications.size() > 1) {
                    clearStoredPurchaseNotifications(groupId);
                    return super.getActivity(context, intent);
                } else {
                    clearStoredPurchaseNotifications(groupId);
                    return PurchaseDetailsActivity.class;
                }
            case TYPE_COMPENSATION_AMOUNT_CHANGED:
                // fall through
            case TYPE_SETTLEMENT_NEW:
                // fall through
            case TYPE_COMPENSATION_REMIND_USER:
                // fall through
            case TYPE_COMPENSATION_EXISTING_NOT_NOW:
                // fall through
            case TYPE_COMPENSATION_REMIND_USER_HAS_PAID:
                return CompensationsActivity.class;
            default:
                return super.getActivity(context, intent);
        }
    }

    /**
     * Clears stored notifications in sharedPreferences
     */
    private void clearStoredPurchaseNotifications(String groupId) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(STORED_PURCHASE_NOTIFICATIONS + groupId);
        editor.apply();
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        String type = getNotificationType(intent);
        if (type.equals(TYPE_PURCHASE_NEW)) {
            String groupId = "";
            JSONObject jsonExtras = null;
            try {
                jsonExtras = getData(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (jsonExtras != null) {
                groupId = jsonExtras.optString(PUSH_PARAM_GROUP);
            }

            clearStoredPurchaseNotifications(groupId);
        }

        super.onPushDismiss(context, intent);
    }
}
