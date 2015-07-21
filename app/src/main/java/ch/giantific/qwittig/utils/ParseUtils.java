package ch.giantific.qwittig.utils;

import com.parse.ParseACL;
import com.parse.ParseConfig;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 27.03.15.
 */
public class ParseUtils {

    private ParseUtils() {
        // class cannot be instantiated
    }

    public static String getGroupCurrency() {
        Currency fallback = Currency.getInstance(Locale.getDefault());
        String currencyCode = fallback.getCurrencyCode();

        Group currentGroup = getCurrentGroup();
        if (currentGroup != null) {
            return currentGroup.getCurrency();
        }

        return currencyCode;
    }

    private static Group getCurrentGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        Group currentGroup = null;
        if (currentUser != null) {
            currentGroup = currentUser.getCurrentGroup();
        }
        return currentGroup;
    }

    public static boolean isTestUser(ParseUser parseUser) {
        User user = (User) parseUser;
        String username = user.getUsername();
        return username.startsWith(User.USERNAME_PREFIX_TEST);
    }

    public static ParseACL getDefaultAcl(ParseObject group) {
        String roleName = getGroupRoleName(group);
        ParseACL acl = new ParseACL();
        acl.setRoleReadAccess(roleName, true);
        acl.setRoleWriteAccess(roleName, true);
        return acl;
    }

    public static String getGroupRoleName(ParseObject group) {
        return Group.ROLE_PREFIX + group.getObjectId();
    }

    public static List<ch.giantific.qwittig.data.models.Currency> getSupportedCurrencies() {
        ParseConfig config = ParseConfig.getCurrentConfig();
        List<String> currencyCodes = config.getList(Config.SUPPORTED_CURRENCIES);
        List<String> currencyNames = MoneyUtils.getCurrencyDisplayNames(currencyCodes);
        List<ch.giantific.qwittig.data.models.Currency> currencies = new ArrayList<>();

        for (int i = 0, currencyNamesLength = currencyNames.size(); i < currencyNamesLength; i++) {
            currencies.add(new ch.giantific.qwittig.data.models.Currency(currencyNames.get(i), currencyCodes.get(i)));
        }

        return currencies;
    }

    public static List<String> getSupportedCurrencyCodes() {
        ParseConfig config = ParseConfig.getCurrentConfig();

        return config.getList(Config.SUPPORTED_CURRENCIES);
    }
}
