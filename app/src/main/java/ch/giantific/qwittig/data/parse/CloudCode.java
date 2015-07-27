package ch.giantific.qwittig.data.parse;

import android.content.Context;
import android.support.annotation.NonNull;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.PushBroadcastReceiver;
import ch.giantific.qwittig.utils.ParseErrorHandler;

/**
 * Created by fabio on 05.02.15.
 */
public class CloudCode {

    public static final String PUSH_COMPENSATION_REMIND = "pushCompensationRemind";
    public static final String PUSH_COMPENSATION_REMIND_PAID = "pushCompensationRemindPaid";
    public static final String CALCULATE_BALANCE = "calculateBalance";
    public static final String SETTLEMENT_NEW = "settlementNew";
    public static final String GROUP_ROLE_ADD_USER = "groupRoleAddUser";
    public static final String GROUP_ROLE_REMOVE_USER = "groupRoleRemoveUser";
    public static final String INVITE_USER = "inviteUser";
    public static final String DELETE_PARSE_FILE = "deleteParseFile";
    public static final String STATS_SPENDING = "statsSpending";
    public static final String STATS_STORES = "statsStores";
    public static final String STATS_CURRENCIES = "statsCurrencies";
    public static final String DELETE_ACCOUNT = "deleteAccount";

    private static final String LOG_TAG = CloudCode.class.getSimpleName();
    private static final String PARAM_SETTLEMENT_SINGLE_USER = "singleUser";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_FILE_NAME = "fileName";
    private static final String PARAM_YEAR = "year";
    private static final String PARAM_MONTH = "month";

    private CloudCode() {
        // Class cannot be instantiated
    }

    public static void startNewSettlement(final Context context, ParseObject groupToBalance,
                                          boolean doSingleUserSettlement,
                                          final CloudFunctionListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP, groupToBalance.getObjectId());
        params.put(PARAM_SETTLEMENT_SINGLE_USER, doSingleUserSettlement);
        ParseCloud.callFunctionInBackground(SETTLEMENT_NEW, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(SETTLEMENT_NEW, o);
                }
            }
        });
    }

    public static void pushCompensationRemind(final Context context, String compensationId,
                                              String currencyCode,
                                              final CloudFunctionListener listener) {
        Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);

        ParseCloud.callFunctionInBackground(PUSH_COMPENSATION_REMIND, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(PUSH_COMPENSATION_REMIND, o);
                }
            }
        });
    }

    public static void pushCompensationRemindPaid(final Context context, String compensationId,
                                                  String currencyCode,
                                                  final CloudFunctionListener listener) {
        Map<String, Object> params = getCompensationPushParams(compensationId, currencyCode);

        ParseCloud.callFunctionInBackground(PUSH_COMPENSATION_REMIND_PAID, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(PUSH_COMPENSATION_REMIND_PAID, o);
                }
            }
        });
    }

    private static Map<String, Object> getCompensationPushParams(String compensationId,
                                                                 String currencyCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_COMPENSATION, compensationId);
        params.put(PushBroadcastReceiver.PUSH_PARAM_CURRENCY_CODE, currencyCode);

        return params;
    }

    public static void addUserToGroupRole(final Context context, String groupId,
                                          final CloudFunctionListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP, groupId);
        ParseCloud.callFunctionInBackground(GROUP_ROLE_ADD_USER, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(GROUP_ROLE_ADD_USER, o);
                }
            }
        });
    }

    public static void removeUserFromGroupRole(String groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP, groupId);
        ParseCloud.callFunctionInBackground(GROUP_ROLE_REMOVE_USER, params);
    }

    public static void calculateBalance(final Context context,
                                        final CloudFunctionListener listener) {
        Map<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground(CALCULATE_BALANCE, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(CALCULATE_BALANCE, o);
                }
            }
        });
    }

    public static void inviteUser(Context context, String email, String groupName,
                                  CloudFunctionListener listener) {
        List<String> emails = new ArrayList<>();
        emails.add(email);
        inviteUsers(context, emails, groupName, listener);
    }

    public static void inviteUsers(final Context context, List<String> emails, String groupName,
                                  final CloudFunctionListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_EMAIL, emails);
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP_NAME, groupName);
        ParseCloud.callFunctionInBackground(INVITE_USER, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(INVITE_USER, o);
                }
            }
        });
    }

    public static void deleteParseFile(final Context context, String fileName,
                                       final CloudFunctionListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_FILE_NAME, fileName);
        ParseCloud.callFunctionInBackground(DELETE_PARSE_FILE, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(DELETE_PARSE_FILE, o);
                }
            }
        });
    }

    public static void statsSpending(final Context context,
                                     final CloudFunctionListener listener, String groupId,
                                     String year, int month) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        ParseCloud.callFunctionInBackground(STATS_SPENDING, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(STATS_SPENDING, o);
                }
            }
        });
    }

    public static void statsStores(final Context context,
                                   final CloudFunctionListener listener, String groupId,
                                   String year, int month) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        ParseCloud.callFunctionInBackground(STATS_STORES, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(STATS_STORES, o);
                }
            }
        });
    }

    public static void statsCurrencies(final Context context,
                                   final CloudFunctionListener listener, String groupId,
                                   String year, int month) {
        Map<String, Object> params = getStatsPushParams(groupId, year, month);
        ParseCloud.callFunctionInBackground(STATS_CURRENCIES, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(STATS_CURRENCIES, o);
                }
            }
        });
    }

    @NonNull
    private static Map<String, Object> getStatsPushParams(String groupId, String year, int month) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP, groupId);
        params.put(PARAM_YEAR, year);
        if (month != 0) {
            params.put(PARAM_MONTH, month - 1);
        }
        return params;
    }

    public static void deleteAccount(final Context context, final CloudFunctionListener listener) {
        Map<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground(DELETE_ACCOUNT, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(context, e);
                    if (listener != null) {
                        listener.onCloudFunctionError(e);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onCloudFunctionReturned(DELETE_ACCOUNT, o);
                }
            }
        });
    }

    public interface CloudFunctionListener {
        void onCloudFunctionError(ParseException e);
        void onCloudFunctionReturned(String cloudFunction, Object o);
    }
}
