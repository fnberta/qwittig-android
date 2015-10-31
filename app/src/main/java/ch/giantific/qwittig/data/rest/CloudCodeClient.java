/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.rest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.receivers.PushBroadcastReceiver;

/**
 * Provides access to server functions implemented in the Parse.com Cloud Code framework.
 */
public class CloudCodeClient {

    public static final String CALCULATE_BALANCE = "calculateBalance";
    public static final String INVITE_USER = "inviteUsers";
    public static final String PUSH_TASK_REMIND = "pushTaskRemind";
    public static final String PUSH_COMPENSATION_REMIND = "pushCompensationRemind";
    public static final String PUSH_COMPENSATION_REMIND_PAID = "pushCompensationRemindPaid";
    public static final String SETTLEMENT_NEW = "settlementNew";
    public static final String GROUP_ROLE_ADD_USER = "groupRoleAddUser";
    public static final String GROUP_ROLE_REMOVE_USER = "groupRoleRemoveUser";
    public static final String DELETE_PARSE_FILE = "deleteParseFile";
    public static final String DELETE_ACCOUNT = "deleteAccount";
    public static final String STATS_SPENDING = "statsSpending";
    public static final String STATS_STORES = "statsStores";
    public static final String STATS_CURRENCIES = "statsCurrencies";

    public static final String PARAM_SETTLEMENT_SINGLE_USER = "singleUser";
    public static final String PARAM_EMAIL = "emails";
    public static final String PARAM_FILE_NAME = "fileName";
    public static final String PARAM_YEAR = "year";
    public static final String PARAM_MONTH = "month";

    public CloudCodeClient() {
    }

    private void onFunctionReturned(Object o, @Nullable ParseException e,
                                    @NonNull CloudCodeListener listener) {
        if (e != null) {
            listener.onCloudFunctionFailed(e.getCode());
            return;
        }

        listener.onCloudFunctionReturned(o);
    }

    /**
     * Re-calculates the balances all users of the current user's groups.
     *
     * @param listener the callback for when the Cloud Code function returns
     */
    public void calcUserBalances(@NonNull final CloudCodeListener listener) {
        Map<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground(CALCULATE_BALANCE, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Invites new users to join a group by sending them an email and a push notification if the
     * there is already an account for the email address.
     *
     * @param emails    the emails to which to send the invitations too
     * @param groupName the name of the group the users are invited to
     * @param listener  the callback for when the Cloud Code function returns
     */
    public void inviteUsers(@NonNull List<String> emails, @NonNull String groupName,
                            @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_EMAIL, emails);
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_NAME, groupName);
        ParseCloud.callFunctionInBackground(INVITE_USER, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Sends a push notification to remind a user to finish a task.
     *
     * @param taskId   the object id of the task that should be finished
     * @param listener the callback for when the Cloud Code function returns
     */
    public void pushTaskReminder(@NonNull final String taskId,
                                 @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_TASK_ID, taskId);
        ParseCloud.callFunctionInBackground(PUSH_TASK_REMIND, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Sends a push notification to remind a user to pay a compensation.
     *
     * @param compensationId the object id of the compensation that needs to be paid
     * @param currencyCode   the currency code to format the price in the push notification
     * @param listener       the callback for when the Cloud Code function returns
     */
    public void pushCompensationReminder(@NonNull final String compensationId,
                                         @NonNull String currencyCode,
                                         @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);
        ParseCloud.callFunctionInBackground(PUSH_COMPENSATION_REMIND, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Sends a push notification to remind a user to mark a payment as accepted.
     *
     * @param compensationId the object id of the compensation that should be marked as accepted
     * @param currencyCode   the currency code to format the price in the push notification
     * @param listener       the callback for when the Cloud Code function returns
     */
    public void pushCompensationPaidReminder(@NonNull final String compensationId,
                                             @NonNull String currencyCode,
                                             @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);
        ParseCloud.callFunctionInBackground(PUSH_COMPENSATION_REMIND_PAID, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);

            }
        });
    }

    @NonNull
    private Map<String, Object> getCompensationPushParams(@NonNull String compensationId,
                                                          @NonNull String currencyCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_COMPENSATION_ID, compensationId);
        params.put(PushBroadcastReceiver.PUSH_PARAM_CURRENCY_CODE, currencyCode);

        return params;
    }

    /**
     * Calculates a new settlement and pushes the result to all users in the group.
     *
     * @param groupToBalanceId       the object id of the group to settle
     * @param doSingleUserSettlement whether only the current user's balance should be settled or
     *                               that of all users
     * @param listener               the callback for when the Cloud Code function returns
     */
    public void startNewSettlement(@NonNull String groupToBalanceId,
                                   boolean doSingleUserSettlement,
                                   @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID, groupToBalanceId);
        params.put(PARAM_SETTLEMENT_SINGLE_USER, doSingleUserSettlement);
        ParseCloud.callFunctionInBackground(SETTLEMENT_NEW, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Adds the current user to the group role, giving him/her access to all the objects of the
     * group.
     *
     * @param groupId  the object id of the group to whose role the user should be added
     * @param listener the callback for when the Cloud Code function returns
     */
    public void addUserToGroupRole(@NonNull final String groupId,
                                   @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID, groupId);
        ParseCloud.callFunctionInBackground(GROUP_ROLE_ADD_USER, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Removes the current user from the group role, preventing him from accessing any objects that
     * belong to the group.
     *
     * @param groupId the object id of the group from whose role the user should be removed
     */
    public void removeUserFromGroupRole(@NonNull final String groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID, groupId);
        ParseCloud.callFunctionInBackground(GROUP_ROLE_REMOVE_USER, params);
    }

    /**
     * Deletes the the specified {@link ParseFile}, probably a receipt image that is no longer
     * needed.
     *
     * @param fileName the file name of the {@link ParseFile} to delete
     * @param listener the callback for when the Cloud Code function returns
     */
    public void deleteParseFile(@NonNull String fileName,
                                @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_FILE_NAME, fileName);
        ParseCloud.callFunctionInBackground(DELETE_PARSE_FILE, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Deletes a users account by setting all his/her fields to empty. Does not actually delete
     * the user because that would cause all purchases the users is involved in to be corrupted.
     *
     * @param listener the callback for when the Cloud Code function returns
     */
    public void deleteAccount(@NonNull final CloudCodeListener listener) {
        Map<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground(DELETE_ACCOUNT, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Calculates the stats that show how much each users of the group spent in a month/year.
     *
     * @param groupId  the object id of the group to calculate stats for
     * @param year     the year for which to calculate stats for
     * @param month    the month for which to calculate stats for, 0 means the whole year
     * @param listener the callback for when the Cloud Code function returns
     */
    public void calcStatsSpending(@NonNull String groupId, @NonNull String year, int month,
                                  @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        ParseCloud.callFunctionInBackground(STATS_SPENDING, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Calculates the stats that shows the percentages of stores used in purchases.
     *
     * @param groupId  the object id of the group to calculate stats for
     * @param year     the year for which to calculate stats for
     * @param month    the month for which to calculate stats for, 0 means the whole year
     * @param listener the callback for when the Cloud Code function returns
     */
    public void calcStatsStores(@NonNull String groupId, @NonNull String year, int month,
                                @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        ParseCloud.callFunctionInBackground(STATS_STORES, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    /**
     * Calculates the stats that the percentages of currencies used in purchases.
     *
     * @param groupId  the object id of the group to calculate stats for
     * @param year     the year for which to calculate stats for
     * @param month    the month for which to calculate stats for, 0 means the whole year
     * @param listener the callback for when the Cloud Code function returns
     */
    public void calcStatsCurrencies(@NonNull String groupId, @NonNull String year, int month,
                                    @NonNull final CloudCodeListener listener) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        ParseCloud.callFunctionInBackground(STATS_CURRENCIES, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                onFunctionReturned(o, e, listener);
            }
        });
    }

    @NonNull
    private Map<String, Object> getStatsPushParams(@NonNull String groupId, @NonNull String year,
                                                   int month) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_ID, groupId);
        params.put(PARAM_YEAR, year);
        if (month != 0) {
            params.put(PARAM_MONTH, month - 1);
        }
        return params;
    }

    /**
     * Defines the actions to the take when a Cloud Code functions retuns.
     */
    public interface CloudCodeListener {

        /**
         * Handles the case when the function returned successfully.
         *
         * @param result the result sent back from the function
         */
        void onCloudFunctionReturned(Object result);

        /**
         * Handles the case the function returned with an error.
         *
         * @param errorCode the error code of the exception thrown by the function
         */
        void onCloudFunctionFailed(int errorCode);
    }
}
