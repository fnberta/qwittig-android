/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.receivers;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.services.ParseQueryService;
import ch.giantific.qwittig.di.components.DaggerPushReceiverComponent;
import ch.giantific.qwittig.di.modules.SystemServiceModule;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.finance.FinanceActivity;
import ch.giantific.qwittig.presentation.home.HomeActivity;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsActivity;
import ch.giantific.qwittig.presentation.tasks.details.TaskDetailsActivity;
import ch.giantific.qwittig.presentation.tasks.list.TasksActivity;
import ch.giantific.qwittig.utils.MoneyUtils;
import timber.log.Timber;

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
    public static final String PUSH_PARAM_DEBTOR_ID = "debtorId";
    public static final String PUSH_PARAM_CREDITOR_ID = "creditorId";
    public static final String PUSH_PARAM_BUYER_ID = "buyerId";
    public static final String PUSH_PARAM_INITIATOR_ID = "initiatorId";
    public static final String PUSH_PARAM_TASK_ID = "taskId";
    public static final String PUSH_PARAM_IDENTITIES_IDS = "identitiesIds";
    public static final String PUSH_PARAM_USER = "user";
    public static final String PUSH_PARAM_AMOUNT = "amount";
    public static final String PUSH_PARAM_STORE = "store";
    public static final String PUSH_PARAM_GROUP_NAME = "groupName";
    public static final String PUSH_PARAM_CURRENCY_CODE = "currencyCode";
    public static final String PUSH_PARAM_TASK_TITLE = "taskTitle";

    public static final String INTENT_EXTRA_FINANCE_FRAGMENT = "INTENT_EXTRA_FINANCE_FRAGMENT";

    private static final String TYPE_PURCHASE_NEW = "purchaseNew";
    private static final String TYPE_PURCHASE_EDIT = "purchaseEdit";
    private static final String TYPE_PURCHASE_DELETE = "purchaseDelete";
    private static final String TYPE_COMPENSATION_EXISTING_PAID = "compensationExistingPaid";
    private static final String TYPE_COMPENSATION_REMIND_USER = "compensationRemindUser";
    private static final String TYPE_COMPENSATION_REMIND_USER_HAS_PAID = "compensationRemindUserHasPaid";
    private static final String TYPE_USER_JOINED = "userJoined";
    private static final String TYPE_USER_LEFT = "userLeft";
    private static final String TYPE_USER_DELETED = "userDeleted";
    private static final String TYPE_GROUP_NAME_CHANGED = "groupNameChanged";
    private static final String TYPE_TASK_NEW = "taskNew";
    private static final String TYPE_TASK_DELETE = "taskDelete";
    private static final String TYPE_TASK_EDIT = "taskEdit";
    private static final String TYPE_TASK_REMIND_USER = "taskRemindUser";
    private static final String ACTION_PUSH_BUTTON_ACCEPT = "ch.giantific.qwittig.push.intent.ACCEPT";
    private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final int NEW_PURCHASE_NOTIFICATION_ID = 1;
    private static final String STORED_PURCHASE_NOTIFICATIONS = "STORED_PURCHASE_NOTIFICATIONS";
    private static final int MAX_LINES_INBOX_STYLE = 7;
    @Inject
    SharedPreferences mSharedPrefs;
    @Inject
    UserRepository mUserRepo;
    @Inject
    NotificationManager mNotificationManager;
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
        injectDependencies(context);

        final String action = intent.getAction();
        switch (action) {
            case ACTION_PUSH_BUTTON_ACCEPT: {
                JSONObject jsonExtras;
                try {
                    jsonExtras = getData(intent);
                } catch (JSONException e) {
                    super.onReceive(context, intent);
                    return;
                }

                final String type = jsonExtras.optString(NOTIFICATION_TYPE);
                switch (type) {
                    case TYPE_COMPENSATION_REMIND_USER_HAS_PAID: {
                        final Compensation compensation = getCompensation(jsonExtras);
                        compensation.setPaid(true);
                        compensation.saveEventually();

                        cancelNotification(intent);
                        break;
                    }
                    case TYPE_TASK_REMIND_USER:
                        final String taskId = jsonExtras.optString(PUSH_PARAM_TASK_ID);
                        ParseQueryService.startTaskDone(context, taskId);

                        cancelNotification(intent);
                }
                break;
            }
            default:
                super.onReceive(context, intent);
        }
    }

    private void injectDependencies(@NonNull Context context) {
        DaggerPushReceiverComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(context))
                .systemServiceModule(new SystemServiceModule(context))
                .build()
                .inject(this);
    }

    @NonNull
    private Compensation getCompensation(@NonNull JSONObject jsonExtras) {
        final String compensationId = jsonExtras.optString(PUSH_PARAM_COMPENSATION_ID);
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
        Timber.e("onPushReceive");

        final User currentUser = mUserRepo.getCurrentUser();
        // return immediately if no user is logged in
        if (currentUser == null) {
            Timber.e("currentUser is null");
            return;
        }
        final List<Identity> userIdentities = currentUser.getIdentities();

        JSONObject jsonExtras;
        try {
            jsonExtras = getData(intent);
        } catch (JSONException e) {
            // return immediately if no data
            Timber.e("jsonExtras is null");
            return;
        }

        int notificationId = (int) System.currentTimeMillis();
        String notificationTag = null;
        final String type = getNotificationType(intent);
        switch (type) {
            case TYPE_PURCHASE_NEW: {
                // set notification id and tag
                notificationId = NEW_PURCHASE_NOTIFICATION_ID;
                notificationTag = jsonExtras.optString(PUSH_PARAM_GROUP_ID);

                // update balance for all identities
                ParseQueryService.startQueryIdentities(context);

                // query purchase only for identities in purchase's identities
                final List<String> identitiesIds = getIdentitiesIds(jsonExtras);
                if (!currentUser.hasIdentity(identitiesIds)) {
                    return;
                }
                final String purchaseId = jsonExtras.optString(PUSH_PARAM_PURCHASE_ID);
                ParseQueryService.startQueryObject(context, Purchase.CLASS, purchaseId, true);

                // don't show notification for buyer
                final String buyerId = jsonExtras.optString(PUSH_PARAM_BUYER_ID);
                for (Identity identity : userIdentities) {
                    if (buyerId.equals(identity.getObjectId())) {
                        return;
                    }
                }

                // store text in LinkedHashSet for combined notifications
                final String store = jsonExtras.optString(PUSH_PARAM_STORE);
                final String totalAmountFormatted = getAmount(jsonExtras);
                mPurchaseNotifications = mSharedPrefs.getStringSet(
                        STORED_PURCHASE_NOTIFICATIONS + notificationTag, new LinkedHashSet<String>());
                mPurchaseNotifications.add(String.format(Locale.getDefault(), "%s: %s", store,
                        totalAmountFormatted));

                // save set in sharedPreferences
                mSharedPrefs.edit()
                        .putStringSet(STORED_PURCHASE_NOTIFICATIONS + notificationTag, mPurchaseNotifications)
                        .apply();
                break;
            }
            case TYPE_PURCHASE_EDIT: {
                // update balance for all identities
                ParseQueryService.startQueryIdentities(context);

                // only update purchase for identities in purchase's identities
                final List<String> identitiesIds = getIdentitiesIds(jsonExtras);
                if (!currentUser.hasIdentity(identitiesIds)) {
                    return;
                }
                final String purchaseId = jsonExtras.optString(PUSH_PARAM_PURCHASE_ID);
                ParseQueryService.startQueryObject(context, Purchase.CLASS, purchaseId, false);
                break;
            }
            case TYPE_PURCHASE_DELETE: {
                // update balance for all identities
                ParseQueryService.startQueryIdentities(context);

                // only unpin local purchase for identities in purchase's identities
                final List<String> identitiesIds = getIdentitiesIds(jsonExtras);
                if (!currentUser.hasIdentity(identitiesIds)) {
                    return;
                }
                final String purchaseId = jsonExtras.optString(PUSH_PARAM_PURCHASE_ID);
                final String groupId = jsonExtras.optString(PUSH_PARAM_GROUP_ID);
                ParseQueryService.startUnpinObject(context, Purchase.CLASS, purchaseId, groupId);
                break;
            }
            case TYPE_COMPENSATION_EXISTING_PAID: {
                // update balance for all users
                ParseQueryService.startQueryIdentities(context);

                // update all compensations, TODO: only all if amount was changed
                ParseQueryService.startQueryCompensations(context);

                // update compensation for all users
//                queryCompensation(context, jsonExtras, false);

                // only show notification for payer
                final String payerId = jsonExtras.optString(PUSH_PARAM_DEBTOR_ID);
                if (!payerId.equals(currentUser.getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_TASK_NEW: {
                final String taskId = jsonExtras.optString(PUSH_PARAM_TASK_ID);
                ParseQueryService.startQueryObject(context, Task.CLASS, taskId, true);

                // don't show notification for initiator of task
                final String initiatorId = jsonExtras.optString(PUSH_PARAM_INITIATOR_ID);
                if (initiatorId.equals(currentUser.getObjectId())) {
                    return;
                }
                break;
            }
            case TYPE_TASK_EDIT: {
                final String taskId = jsonExtras.optString(PUSH_PARAM_TASK_ID);
                ParseQueryService.startQueryObject(context, Task.CLASS, taskId, false);
                break;
            }
            case TYPE_TASK_DELETE: {
                final String taskId = jsonExtras.optString(PUSH_PARAM_TASK_ID);
                final String groupId = jsonExtras.optString(PUSH_PARAM_GROUP_ID);
                ParseQueryService.startUnpinObject(context, Task.CLASS, taskId, groupId);

                // don't show notification for initiator of task,
                // TODO: actually we don't want to show for the user that deleted the task / finished a one-time task
                final String initiatorId = jsonExtras.optString(PUSH_PARAM_INITIATOR_ID);
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
                ParseQueryService.startQueryIdentities(context);
                break;
            }
            case TYPE_GROUP_NAME_CHANGED: {
                final String groupId = jsonExtras.optString(PUSH_PARAM_GROUP_ID);
                ParseQueryService.startQueryObject(context, Group.CLASS, groupId, false);
                break;

            }
        }

        // get the notification and display it if it's not a silent one
        if (!isSilentNotification(type)) {
            intent.putExtra(NOTIFICATION_ID, notificationId);
            final Notification notification = getNotification(context, intent);
            mNotificationManager.notify(notificationTag, notificationId, notification);
        }
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

    @NonNull
    private List<String> getIdentitiesIds(@NonNull JSONObject jsonExtras) {
        final JSONArray json = jsonExtras.optJSONArray(PUSH_PARAM_IDENTITIES_IDS);
        final List<String> identitiesIds = new ArrayList<>();
        for (int i = 0, length = json.length(); i < length; i++) {
            identitiesIds.add(json.optString(i));
        }
        return identitiesIds;
    }

    private void queryCompensation(@NonNull Context context, @NonNull JSONObject jsonExtras,
                                   boolean isNew) {
        final String compensationId = jsonExtras.optString(PUSH_PARAM_COMPENSATION_ID);
        ParseQueryService.startQueryObject(context, Compensation.CLASS, compensationId, isNew);
    }

    private String getAmount(@NonNull JSONObject jsonExtras) {
        final String currencyCode = jsonExtras.optString(PUSH_PARAM_CURRENCY_CODE);
        double amount = jsonExtras.optDouble(PUSH_PARAM_AMOUNT);
        return MoneyUtils.formatMoney(amount, currencyCode);
    }

    private boolean isSilentNotification(@NonNull String type) {
        return type.equals(TYPE_PURCHASE_EDIT) || type.equals(TYPE_PURCHASE_DELETE) ||
                type.equals(TYPE_GROUP_NAME_CHANGED) || type.equals(TYPE_TASK_EDIT);
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
        final Bundle extras = intent.getExtras();
        final String packageName = context.getPackageName();

        // setup intents
        final Random random = new Random();
        // openIntent
        final Intent contentIntent = new Intent(ACTION_PUSH_OPEN);
        contentIntent.putExtras(extras);
        contentIntent.setPackage(packageName);
        final int contentIntentRequestCode = random.nextInt();
        final PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // deleteIntent
        final Intent deleteIntent = new Intent(ACTION_PUSH_DELETE);
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);
        final int deleteIntentRequestCode = random.nextInt();
        final PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode,
                deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // setup notification builder
        final Notification.Builder builder = new Notification.Builder(context);
        builder.setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(getSmallIconId(context, intent))
                .setAutoCancel(true)
                .setContentIntent(pContentIntent)
                .setDeleteIntent(pDeleteIntent);

        final String user = jsonExtras.optString(PUSH_PARAM_USER);
        String title;
        String alert;
        final String type = getNotificationType(intent);
        switch (type) {
            case TYPE_PURCHASE_NEW: {
                final String store = jsonExtras.optString(PUSH_PARAM_STORE);
                final String totalAmountFormatted = getAmount(jsonExtras);
                title = context.getString(R.string.push_purchase_new_title, user);
                alert = context.getString(R.string.push_purchase_new_alert, store, totalAmountFormatted);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);

                // use inbox style if there are multiple new purchase notifications
                if (mPurchaseNotifications != null && mPurchaseNotifications.size() > 1) {
                    final String groupName = jsonExtras.optString(PUSH_PARAM_GROUP_NAME);

                    // create inboxStyle
                    final Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
                    for (String purchaseNotification : mPurchaseNotifications) {
                        inboxStyle.addLine(purchaseNotification);
                    }
                    int size = mPurchaseNotifications.size();
                    final String summaryText = size > MAX_LINES_INBOX_STYLE
                            ? "+" + String.valueOf(size - MAX_LINES_INBOX_STYLE) + " " +
                            context.getString(R.string.push_purchase_new_multiple_more)
                            : groupName;
                    inboxStyle.setSummaryText(summaryText);

                    // set style, title and text to builder
                    builder.setContentTitle(context.getString(R.string.push_purchase_new_multiple_title, size))
                            .setContentText(groupName)
                            .setStyle(inboxStyle);
                }
                break;
            }
            case TYPE_COMPENSATION_REMIND_USER: {
                final String amount = getAmount(jsonExtras);
                title = context.getString(R.string.push_compensation_remind_title);
                alert = context.getString(R.string.push_compensation_remind_alert, user, amount);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_COMPENSATION_REMIND_USER_HAS_PAID: {
                final String amount = getAmount(jsonExtras);
                title = context.getString(R.string.push_compensation_remind_paid_title, user);
                alert = context.getString(R.string.push_compensation_remind_paid_alert, amount);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);

                // setup action button
                final Intent acceptContentIntent = new Intent(ACTION_PUSH_BUTTON_ACCEPT);
                acceptContentIntent.setPackage(packageName);
                acceptContentIntent.putExtras(extras);
                int acceptContentIntentRequestCode = random.nextInt();
                final PendingIntent pAcceptContentIntent = PendingIntent.getBroadcast(context,
                        acceptContentIntentRequestCode, acceptContentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_done_black_24dp,
                        context.getString(R.string.push_action_confirm), pAcceptContentIntent);
                break;
            }
            case TYPE_COMPENSATION_EXISTING_PAID: {
                final String amount = getAmount(jsonExtras);
                title = context.getString(R.string.push_compensation_payment_done_title);
                alert = context.getString(R.string.push_compensation_payment_done_alert, user, amount);

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
                final String taskTitle = jsonExtras.optString(PUSH_PARAM_TASK_TITLE);
                title = context.getString(R.string.push_task_delete_title, taskTitle);
                alert = context.getString(R.string.push_task_delete_alert, user);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_TASK_REMIND_USER: {
                final String taskTitle = jsonExtras.optString(PUSH_PARAM_TASK_TITLE);
                title = context.getString(R.string.push_task_remind_title, taskTitle);
                alert = context.getString(R.string.push_task_remind_alert, user);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);

                // setup action button
                final Intent acceptContentIntent = new Intent(ACTION_PUSH_BUTTON_ACCEPT);
                acceptContentIntent.setPackage(packageName);
                acceptContentIntent.putExtras(extras);
                int acceptContentIntentRequestCode = random.nextInt();
                final PendingIntent pAcceptContentIntent = PendingIntent.getBroadcast(context,
                        acceptContentIntentRequestCode, acceptContentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_done_black_24dp,
                        context.getString(R.string.push_action_finished), pAcceptContentIntent);
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
                final String groupName = jsonExtras.optString(PUSH_PARAM_GROUP_NAME);
                title = context.getString(R.string.push_user_joined_title, groupName);
                alert = context.getString(R.string.push_user_joined_alert, user);

                // set title and alert
                builder.setContentTitle(title).setContentText(alert);
                break;
            }
            case TYPE_USER_LEFT: {
                final String groupName = jsonExtras.optString(PUSH_PARAM_GROUP_NAME);
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
            final Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle().bigText(alert);
            builder.setStyle(bigTextStyle);
        }

        return builder.build();
    }

    @Override
    protected void onPushOpen(Context context, @NonNull Intent intent) {
        final String type = getNotificationType(intent);
        if (!isSilentNotification(type)) {
            String groupId;
            try {
                groupId = getGroupId(intent);
            } catch (JSONException e) {
                super.onPushOpen(context, intent);
                return;
            }

            final User currentUser = mUserRepo.getCurrentUser();
            if (currentUser != null) {
                final Group oldGroup = currentUser.getCurrentIdentity().getGroup();
                if (!oldGroup.getObjectId().equals(groupId) && currentUser.isInGroup(groupId)) {
                    final Identity identity = currentUser.getIdentityForGroup(groupId);
                    if (identity != null) {
                        currentUser.setCurrentIdentity(identity);
                        currentUser.saveEventually();
                    }
                }
            }

            switch (type) {
                case TYPE_COMPENSATION_REMIND_USER:
                    // fall through
                case TYPE_COMPENSATION_REMIND_USER_HAS_PAID:
                    intent.putExtra(INTENT_EXTRA_FINANCE_FRAGMENT, FinanceActivity.FragmentTabs.COMPS_UNPAID);
                    break;
                case TYPE_COMPENSATION_EXISTING_PAID:
                    intent.putExtra(INTENT_EXTRA_FINANCE_FRAGMENT, FinanceActivity.FragmentTabs.COMPS_PAID);
                    break;
            }
        }

        super.onPushOpen(context, intent);
    }

    private String getGroupId(@NonNull Intent intent) throws JSONException {
        final JSONObject jsonExtras = getData(intent);
        return jsonExtras.optString(PUSH_PARAM_GROUP_ID);
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

                mPurchaseNotifications = mSharedPrefs.getStringSet(
                        STORED_PURCHASE_NOTIFICATIONS + groupId, new LinkedHashSet<String>());
                if (mPurchaseNotifications.size() > 1) {
                    clearStoredPurchaseNotifications(groupId);
                    return HomeActivity.class;
                } else {
                    clearStoredPurchaseNotifications(groupId);

                    final User currentUser = mUserRepo.getCurrentUser();
                    ;
                    if (currentUser != null && currentUser.isInGroup(groupId)) {
                        return PurchaseDetailsActivity.class;
                    }
                    return HomeActivity.class;
                }
            }
            case TYPE_COMPENSATION_REMIND_USER:
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

                final User currentUser = mUserRepo.getCurrentUser();
                ;
                if (currentUser != null && currentUser.isInGroup(groupId)) {
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
        mSharedPrefs.edit()
                .remove(STORED_PURCHASE_NOTIFICATIONS + groupId)
                .apply();
    }

    @Override
    protected void onPushDismiss(Context context, @NonNull Intent intent) {
        final String type = getNotificationType(intent);
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
