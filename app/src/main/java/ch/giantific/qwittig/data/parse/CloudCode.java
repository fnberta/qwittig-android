package ch.giantific.qwittig.data.parse;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.Map;

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
    public static final String PARAM_SETTLEMENT_SINGLE_USER = "singleUser";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_FILE_NAME = "fileName";
    public static final String PARAM_YEAR = "year";
    public static final String PARAM_MONTH = "month";
    private static final String LOG_TAG = CloudCode.class.getSimpleName();

    private CloudCode() {
        // Class cannot be instantiated
    }

    public static void deleteParseFile(String fileName, final CloudFunctionListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_FILE_NAME, fileName);
        ParseCloud.callFunctionInBackground(DELETE_PARSE_FILE, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
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

    public interface CloudFunctionListener {
        void onCloudFunctionError(ParseException e);

        void onCloudFunctionReturned(String cloudFunction, Object o);
    }
}
