package ch.giantific.qwittig.data.parse;

/**
 * Created by fabio on 05.02.15.
 */
public class CloudCode {

    public static final String PUSH_COMPENSATION_REMIND = "pushCompensationRemind";
    public static final String PUSH_COMPENSATION_REMIND_PAID = "pushCompensationRemindPaid";
    public static final String PUSH_TASK_REMIND = "pushTaskRemind";
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
}
