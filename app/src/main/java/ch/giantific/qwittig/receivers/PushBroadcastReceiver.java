/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.receivers;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.services.ParseQueryService;
import ch.giantific.qwittig.ui.activities.FinanceActivity;
import ch.giantific.qwittig.ui.activities.HomeActivity;
import ch.giantific.qwittig.ui.activities.PurchaseDetailsActivity;
import ch.giantific.qwittig.ui.activities.TaskDetailsActivity;
import ch.giantific.qwittig.ui.activities.TasksActivity;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Handles incoming push messages sent through the Parse.com framework which leverages the Google
 * Cloud Messaging service.
 * <p/>
 * Subclass of {@link ParsePushBroadcastReceiver}.
 */
public class PushBroadcastReceiver extends ParsePushBroadcastReceiver {

    public static final String NOTIFICATION_TYPE = "type";
    public static final String PUSH_PARAM_TITLE = "title";
    public static final String PUSH_PARAM_ALERT = "alert";

    public static final String PUSH_PARAM_PURCHASE_ID = "purchaseId";
    public static final String PUSH_PARAM_COMPENSATION_ID = "compensationId";
    public static final String PUSH_PARAM_GROUP_ID = "groupId";
    public static final String PUSH_PARAM_PAYER_ID = "payerId";
    public static final String PUSH_PARAM_BENEFICIARY_ID = "beneficiaryId";
    public static final String PUSH_PARAM_BUYER_ID = "buyerId";
    public static final String PUSH_PARAM_INITIATOR_ID = "initiatorId";
    public static final String PUSH_PARAM_TASK_ID = "taskId";
    public static final String PUSH_PARAM_USERS_INVOLVED_IDS = "usersInvolvedIds";
    public static final String PUSH_PARAM_USER = "user";
    public static final String PUSH_PARAM_AMOUNT = "amount";
    public static final String PUSH_PARAM_STORE = "store";
    public static final String PUSH_PARAM_GROUP_NAME = "groupName";
    public static final String PUSH_PARAM_CURRENCY_CODE = "currencyCode";
    public static final String PUSH_PARAM_TASK_TITLE = "taskTitle";

    public static final String INTENT_EXTRA_FINANCE_FRAGMENT = "INTENT_EXTRA_FINANCE_FRAGMENT";

    public static final String INTENT_ACTION_INVITATION = "INTENT_ACTION_INVITATION";
    public static final int ACTION_INVITATION_ACCEPTED = 1;
    public static final int ACTION_INVITATION_DISCARDED = 2;

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
    private static final String TYPE_TASK_NEW = "taskNew";
    private static final String TYPE_TASK_DELETE = "taskDelete";
    private static final String TYPE_TASK_EDIT = "taskEdit";
    private static final String TYPE_TASK_REMIND_USER = "taskRemindUser";
    private static final String ACTION_PUSH_BUTTON_ACCEPT = "ch.giantific.qwittig.push.intent.ACCEPT";
    private static final String ACTION_PUSH_BUTTON_DISCARD = "ch.giantific.qwittig.push.intent.DISCARD";
    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final int NEW_PURCHASE_NOTIFICATION_ID = 1;
    private static final String STORED_PURCHASE_NOTIFICATIONS = "STORED_PURCHASE_NOTIFICATIONS";
    private static final int MAX_LINES_INBOX_STYLE = 7;
    private static final String LOG_TAG = PushBroadcastReceiver.class.getSimpleName();
    private SharedPreferences mSharedPreferences;
    private NotificationManager mNotificationManager;
    @Nullable
    private Set<String> mPurchaseNotifications;

    /**
     * Returns the specific data of an intent in the extra {@link #KEY_PUSH_DATA}.
     *
     * @param intent the intent from which to extract the data from
     * @return the extracted data as a {@link JSONObject}
     * @throws JSONException if the intent does not contain data in <code>KEY_PUSH_DATA</code>
     */
    public static JSONObject getData(@NonNull Intent intent) throws JSONException {
        JSONObject extras;
        if (intent.hasExtra(KEY_PUSH_DATA)) {
            extras = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
        } else {
            extras = new JSONObject("");
        }
        return extras;
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String action = intent.getAction();
        switch (action) {
            case ACTION_PUSH_BUTTON_ACCEPT: {
                JSONObject jsonExtras;
                try {
                    jsonExtras = getData(intent);
                } catch (JSONException e) {
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
                    case TYPE_TASK_REMIND_USER:
                        String taskId = jsonExtras.optString(PUSH_PARAM_TASK_ID);
                        ParseQueryService.startTaskDone(context, taskId);

                        cancelNotification(intent);
                }
                break;
            }
            case ACTION_PUSH_BUTTON_DISCARD: {
                JSONObject jsonExtras;
                try {
                    jsonExtras = getData(intent);
                } catch (JSONException e) {
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

    @NonNull
    private Compensation getCompensation(@NonNull JSONObject jsonExtras) {
        String compensationId = jsonExtras.optString(PUSH_PARAM_COMPENSATION_ID);
        return (Compensation) ParseObject.createWithoutData(Compensation.CLASS, compensationId);
    }

    private void cancelNotification(@NonNull Intent intent) {
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        mNotificationManager.cancel(notificationId);
    }

    private void closeShade(@NonNull Context context) {
        Intent closeShade = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeShade);
    }

    @Override
    protected void onPushReceive(@NonNull Context context, @NonNull Intent intent) {
        Log.e(LOG_TAG, "onPushReceive");

        final User currentUser = (User) ParseUser.getCurrentUser();
        // return immediately if no user is logged in
        if (currentUser == null) {
            Log.e(LOG_TAG, "currentUser is null");
            return;
        }

        JSONObject jsonExtras;
        try {
            jsonExtras = getData(intent);
        } catch (JSONException e) {
            // return immediately if no data
            Log.e(LOG_TAG, "jsonExtras is null");
            return;
        }

        int notificationId = (int) System.currentTimeMillis();
        String notificationTag = null;
        String type = getNotificationType(intent);
        switch (type) {
            case TYPE_PURCHASE_NEW: {
                // set notification id and tag
                notificationId = NEW_PURCHASE_NOTIFICATION_ID;
                notificationTag = jsonExtras.optString(PUSH_PARAM_GROUP_ID);

                // update balance for all users
                ParseQueryService.startQueryUsers(context);

                // query purchase only for users in purchase's usersInvolved
                if (!userIsInUsersInvolved(currentUser, jsonExtras)) {
                    return;
                }
                String purchaseId = jsonExtras.optString(PUSH_PARAM_PURCHASE_ID);
                ParseQueryService.startQueryObject(context, Purchase.CLASS, purchaseId, true);

                // don't show notification for buyer
                String buyerId = jsonExtras.optString(PUSH_PARAM_BUYER_ID);
                if (buyerId.equals(currentUser.getObjectId())) {
                    return;
                }

                // store text in LinkedHashSet for combined notifications
                String store = jsonExtras.optString(PUSH_PARAM_STORE);
                String totalAmountFormatted = getAmount(jsonExtras);
                mPurchaseNotifications = mSharedPreferences.getStringSet(
                        STORED_PURCHASE_NOTIFICATIONS + notificationTag, new LinkedHashSet<String>());
                mPurchaseNotifications.add(String.format(Locale.getDefault(), "%s: %s",store,
                        totalAmountFormatted));

                // save set in sharedPreferences
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putStringSet(STORED_PURCHASE_NOTIFICATIONS + notificationTag, mPurchaseNotifications);
                editor.apply();
                break;
            }
            case TYPE_PURCHASE_EDIT: {
                // update balance for all users
                ParseQueryService.startQueryUsers(context);

                // only update purchase for users in purchase's usersInvolved
                if (!userIsInUsersInvolved(currentUser, jsonExtras)) {
                    return;
                }
                String purchaseId = jsonExtras.optString(PUSH_PARAM_PURCHASE_ID);
                ParseQueryService.startQueryObject(context, Purchase.CLASS, purchaseId, false);
                break;
            }
            case TYPE_PURCHASE_DELETE: {
                // update balance for all users
                ParseQueryService.startQueryUsers(context);

                // only unpin local purchase for users in purchase's usersInvolved
                if (!userIsInUsersInvolved(currentUser, jsonExtras)) {
                    return;
                }
                String purchaseId = jsonExtras.optString(PUSH_PARAM_PURCHASE_ID);
                String groupId = jsonExtras.optString(PUSH_PARAM_GROUP_ID);
                ParseQueryService.startUnpinObject(context, Purchase.CLASS, purchaseId, groupId);
                break;
            }
            case TYPE_SETTLEMENT_NEW: {
                String initiatorId = jsonExtras.optString(PUSH_PARAM_INITIATOR_ID);
                if (initiatorId.equals(currentUser.getObjectId())) {
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
                ParseQueryService.startQueryUsers(context);

                // query compensation
                queryCompensation(context, jsonExtras, true);
                break;
            }
            case TYPE_COMPENSATION_EXISTING_PAID: {
                // update balance for all users
                ParseQueryService.startQueryUsers(context);

                // update compensation for all users
                queryCompensation(context, jsonExtras, false);

                // only show notification for payer
                String payerId = jsonExtras.optString(PUSH_PARAM_PAYER_ID);
                if (!payerId.equals(currentUser.getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_COMPENSATION_EXISTING_NOT_NOW: {
                // update balance for all users
                ParseQueryService.startQueryUsers(context);

                // unpin compensation for all users
                String compensationId = jsonExtras.optString(PUSH_PARAM_COMPENSATION_ID);
                ParseQueryService.startUnpinObject(context, Compensation.CLASS, compensationId);

                // only show notification for beneficiary
                String beneficiaryId = jsonExtras.optString(PUSH_PARAM_BENEFICIARY_ID);
                if (!beneficiaryId.equals(currentUser.getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_COMPENSATION_AMOUNT_CHANGED: {
                // update compensation for all users
                queryCompensation(context, jsonExtras, false);

                // only show notification for beneficiary
                String beneficiaryId = jsonExtras.optString(PUSH_PARAM_BENEFICIARY_ID);
                if (!beneficiaryId.equals(currentUser.getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_TASK_NEW: {
                String taskId = jsonExtras.optString(PUSH_PARAM_TASK_ID);
                ParseQueryService.startQueryObject(context, Task.CLASS, taskId, true);

                // don't show notification for initiator of task
                String initiatorId = jsonExtras.optString(PUSH_PARAM_INITIATOR_ID);
                if (initiatorId.equals(currentUser.getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_TASK_EDIT: {
                String taskId = jsonExtras.optString(PUSH_PARAM_TASK_ID);
                ParseQueryService.startQueryObject(context, Task.CLASS, taskId, false);
                break;
            }
            case TYPE_TASK_DELETE: {
                String taskId = jsonExtras.optString(PUSH_PARAM_TASK_ID);
                String groupId = jsonExtras.optString(PUSH_PARAM_GROUP_ID);
                ParseQueryService.startUnpinObject(context, Task.CLASS, taskId, groupId);

                // don't show notification for initiator of task,
                // TODO: actually we don't want to show for the user that deleted the task / finished a one-time task
                String initiatorId = jsonExtras.optString(PUSH_PARAM_INITIATOR_ID);
                if (initiatorId.equals(currentUser.getObjectId())) {
                    return;
                }

                // don't show notification if task was one-time
                break;
            }
            case TYPE_USER_DELETED: {
                ParseQueryService.startQueryAll(context);
                break;
            }
            case TYPE_USER_JOINED:
                // fall through
            case TYPE_USER_LEFT: {
                ParseQueryService.startQueryUsers(context);
                break;
            }
            case TYPE_GROUP_NAME_CHANGED:
                // fall through
            case TYPE_GROUP_USERS_INVITED_CHANGED: {
                String groupId = jsonExtras.optString(PUSH_PARAM_GROUP_ID);
                ParseQueryService.startQueryObject(context, Group.CLASS, groupId, false);
                break;
            }
        }

        // get the notification and display it if it's not a silent one
        if (!isSilentNotification(type)) {
            intent.putExtra(NOTIFICATION_ID, notificationId);
            Notification notification = getNotification(context, intent);
            mNotificationManager.notify(notificationTag, notificationId, notification);
        }
    }

    private void queryCompensation(@NonNull Context context, @NonNull JSONObject jsonExtras, boolean isNew) {
        String compensationId = jsonExtras.optString(PUSH_PARAM_COMPENSATION_ID);
        ParseQueryService.startQueryObject(context, Compensation.CLASS, compensationId, isNew);
    }

    private String getAmount(@NonNull JSONObject jsonExtras) {
        String currencyCode = jsonExtras.optString(PUSH_PARAM_CURRENCY_CODE);
        double amount = jsonExtras.optDouble(PUSH_PARAM_AMOUNT);
        return MoneyUtils.formatMoney(amount, currencyCode);
    }

    private boolean isSilentNotification(@NonNull String type) {
        return type.equals(TYPE_PURCHASE_EDIT) || type.equals(TYPE_PURCHASE_DELETE) ||
                type.equals(TYPE_COMPENSATION_NEW_PAID) || type.equals(TYPE_COMPENSATION_NEW_UNPAID) ||
                type.equals(TYPE_GROUP_NAME_CHANGED) || type.equals(TYPE_GROUP_USERS_INVITED_CHANGED) ||
                type.equals(TYPE_TASK_EDIT);
    }

    private String getNotificationType(@NonNull Intent intent) {
        JSONObject jsonExtras;
        try {
            jsonExtras = getData(intent);
            return jsonExtras.optString(NOTIFICATION_TYPE);
        } catch (JSONException e) {
            return "";
        }
    }

    private boolean userIsInUsersInvolved(@NonNull User user, @NonNull JSONObject jsonExtras) {
        JSONArray usersInvolvedIds = jsonExtras.optJSONArray(PUSH_PARAM_USERS_INVOLVED_IDS);
        return usersInvolvedIds.toString().contains(user.getObjectId());
    }

    @Override
    protected Notification getNotification(@NonNull Context context, @NonNull Intent intent) {
        JSONObject jsonExtras;
        try {
            jsonExtras = getData(intent);
        } catch (JSONException e) {
            // fall back to super implementation if there is no data
            return super.getNotification(context, intent);
        }

        // if push has non empty title and/or alert, fall back to super implementation
        if (jsonExtras.has(PUSH_PARAM_TITLE) || jsonExtras.has(PUSH_PARAM_ALERT)) {
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
                String totalAmountFormatted = getAmount(jsonExtras);
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
                    builder.setContentTitle(context.getString(R.string.push_purchase_new_multiple_title, size))
                            .setContentText(groupName)
                            .setStyle(inboxStyle);
                }
                break;
            }
            case TYPE_SETTLEMENT_NEW:
                title = context.getString(R.string.push_settlement_new_title, user);
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
                builder.addAction(R.drawable.ic_done_black_24dp,
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
            case TYPE_TASK_NEW: {
                title = jsonExtras.optString(PUSH_PARAM_TASK_TITLE);
                alert = context.getString(R.string.push_task_new_alert, user);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_TASK_DELETE: {
                String taskTitle = jsonExtras.optString(PUSH_PARAM_TASK_TITLE);
                title = context.getString(R.string.push_task_delete_title, taskTitle);
                alert = context.getString(R.string.push_task_delete_alert, user);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_TASK_REMIND_USER: {
                String taskTitle = jsonExtras.optString(PUSH_PARAM_TASK_TITLE);
                title = context.getString(R.string.push_task_remind_title, taskTitle);
                alert = context.getString(R.string.push_task_remind_alert, user);

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
                builder.addAction(R.drawable.ic_done_black_24dp,
                        context.getString(R.string.push_action_finished), pAcceptContentIntent);
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
                builder.addAction(R.drawable.ic_done_black_24dp,
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

    @Override
    protected void onPushOpen(Context context, @NonNull Intent intent) {
        String type = getNotificationType(intent);
        if (!isSilentNotification(type) && !type.equals(TYPE_USER_INVITED)) {
            String groupId;
            try {
                groupId = getGroupId(intent);
            } catch (JSONException e) {
                super.onPushOpen(context, intent);
                return;
            }

            final User currentUser = (User) ParseUser.getCurrentUser();
            if (currentUser != null) {
                final Group oldGroup = currentUser.getCurrentGroup();
                if (!oldGroup.getObjectId().equals(groupId) &&
                        isInPurchaseGroup(currentUser, groupId)) {
                    ParseObject group = ParseObject.createWithoutData(Group.CLASS, groupId);
                    currentUser.setCurrentGroup(group);
                    currentUser.saveEventually();
                }
            }

            switch (type) {
                case TYPE_COMPENSATION_AMOUNT_CHANGED:
                    // fall through
                case TYPE_SETTLEMENT_NEW:
                    // fall through
                case TYPE_COMPENSATION_REMIND_USER:
                    // fall through
                case TYPE_COMPENSATION_EXISTING_NOT_NOW:
                    // fall through
                case TYPE_COMPENSATION_REMIND_USER_HAS_PAID:
                    intent.putExtra(INTENT_EXTRA_FINANCE_FRAGMENT, FinanceActivity.TAB_COMPS_UNPAID);
                    break;
                case TYPE_COMPENSATION_EXISTING_PAID:
                    intent.putExtra(INTENT_EXTRA_FINANCE_FRAGMENT, FinanceActivity.TAB_COMPS_PAID);
                    break;
            }
        }

        super.onPushOpen(context, intent);
    }

    private String getGroupId(@NonNull Intent intent) throws JSONException {
        JSONObject jsonExtras = getData(intent);
        return jsonExtras.optString(PUSH_PARAM_GROUP_ID);
    }

    private boolean isInPurchaseGroup(@NonNull User currentUser, String purchaseGroupId) {
        List<String> groupIds = currentUser.getGroupIds();
        return groupIds.contains(purchaseGroupId);
    }

    @Override
    protected Class<? extends Activity> getActivity(@NonNull Context context, @NonNull Intent intent) {
        String type = getNotificationType(intent);
        switch (type) {
            case TYPE_PURCHASE_NEW: {
                String groupId;
                try {
                    groupId = getGroupId(intent);
                } catch (JSONException e) {
                    return HomeActivity.class;
                }

                mPurchaseNotifications = mSharedPreferences.getStringSet(
                        STORED_PURCHASE_NOTIFICATIONS + groupId, new LinkedHashSet<String>());
                if (mPurchaseNotifications.size() > 1) {
                    clearStoredPurchaseNotifications(groupId);
                    return HomeActivity.class;
                } else {
                    clearStoredPurchaseNotifications(groupId);

                    final User currentUser = (User) ParseUser.getCurrentUser();
                    if (currentUser != null && isInPurchaseGroup(currentUser, groupId)) {
                        return PurchaseDetailsActivity.class;
                    }
                    return HomeActivity.class;
                }
            }
            case TYPE_COMPENSATION_AMOUNT_CHANGED:
                // fall through
            case TYPE_SETTLEMENT_NEW:
                // fall through
            case TYPE_COMPENSATION_REMIND_USER:
                // fall through
            case TYPE_COMPENSATION_EXISTING_NOT_NOW:
                // fall through
            case TYPE_COMPENSATION_EXISTING_PAID:
                // fall through
            case TYPE_COMPENSATION_REMIND_USER_HAS_PAID:
                return FinanceActivity.class;
            case TYPE_TASK_NEW:
                // fall through
            case TYPE_TASK_REMIND_USER: {
                String groupId;
                try {
                    groupId = getGroupId(intent);
                } catch (JSONException e) {
                    return TasksActivity.class;
                }

                final User currentUser = (User) ParseUser.getCurrentUser();
                if (currentUser != null && isInPurchaseGroup(currentUser, groupId)) {
                    return TaskDetailsActivity.class;
                }

                return TasksActivity.class;
            }
            case TYPE_TASK_DELETE:
                return TasksActivity.class;
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
    protected void onPushDismiss(Context context, @NonNull Intent intent) {
        String type = getNotificationType(intent);
        if (type.equals(TYPE_PURCHASE_NEW)) {
            String groupId;
            try {
                groupId = getGroupId(intent);
            } catch (JSONException e) {
                super.onPushDismiss(context, intent);
                return;
            }

            clearStoredPurchaseNotifications(groupId);
        }

        super.onPushDismiss(context, intent);
    }
}
