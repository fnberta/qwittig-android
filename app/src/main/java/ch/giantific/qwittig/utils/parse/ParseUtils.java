/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils.parse;

import android.support.annotation.NonNull;

import com.parse.ParseACL;
import com.parse.ParseConfig;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides useful static utility methods related to the Parse.com framework.
 */
public class ParseUtils {

    private ParseUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns a default {@link ParseACL} with read/write access for the role of the passed group.
     *
     * @param group the group to get the role from
     * @return a default {@link ParseACL}
     */
    @NonNull
    public static ParseACL getDefaultAcl(@NonNull Group group) {
        String roleName = getGroupRoleName(group);
        ParseACL acl = new ParseACL();
        acl.setRoleReadAccess(roleName, true);
        acl.setRoleWriteAccess(roleName, true);
        return acl;
    }

    @NonNull
    private static String getGroupRoleName(@NonNull Group group) {
        return Group.ROLE_PREFIX + group.getObjectId();
    }

    /**
     * Returns the currently supported currencies as
     * {@link ch.giantific.qwittig.domain.models.Currency} objects with a name and currency code.
     * Reads the information from {@link ParseConfig}.
     *
     * @return the currently supported currencies
     */
    @NonNull
    public static List<ch.giantific.qwittig.domain.models.Currency> getSupportedCurrencies() {
        ParseConfig config = ParseConfig.getCurrentConfig();
        List<String> currencyCodes = config.getList(ParseConfigUtils.SUPPORTED_CURRENCIES);
        List<String> currencyNames = MoneyUtils.getCurrencyDisplayNames(currencyCodes);

        int currencyNamesLength = currencyNames.size();
        List<ch.giantific.qwittig.domain.models.Currency> currencies =
                new ArrayList<>(currencyNamesLength);
        for (int i = 0; i < currencyNamesLength; i++) {
            currencies.add(new ch.giantific.qwittig.domain.models.Currency(currencyNames.get(i),
                    currencyCodes.get(i)));
        }

        return currencies;
    }

    /**
     * Returns the currently supported currency codes. Reads the information from
     * {@link ParseConfig}.
     *
     * @return the currently supported currency codes
     */
    public static List<String> getSupportedCurrencyCodes() {
        final ParseConfig config = ParseConfig.getCurrentConfig();

        return config.getList(ParseConfigUtils.SUPPORTED_CURRENCIES, new ArrayList<String>());
    }
}
