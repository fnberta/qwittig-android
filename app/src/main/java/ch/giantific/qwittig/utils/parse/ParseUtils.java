/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils.parse;

import android.support.annotation.NonNull;

import com.parse.ParseACL;
import com.parse.ParseConfig;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.presentation.settings.addgroup.Currency;
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
     * @param roleWriteAccess whether to give the group role write access
     * @return a default {@link ParseACL}
     */
    @NonNull
    public static ParseACL getDefaultAcl(@NonNull Group group, boolean roleWriteAccess) {
        final String roleName = getGroupRoleName(group);
        final ParseACL acl = new ParseACL();
        acl.setRoleReadAccess(roleName, true);
        acl.setRoleWriteAccess(roleName, roleWriteAccess);
        return acl;
    }

    @NonNull
    private static String getGroupRoleName(@NonNull Group group) {
        return Group.ROLE_PREFIX + group.getObjectId();
    }

    /**
     * Returns the currently supported currencies as
     * {@link Currency} objects with a name and currency code.
     * Reads the information from {@link ParseConfig}.
     *
     * @return the currently supported currencies
     */
    @NonNull
    public static List<Currency> getSupportedCurrencies() {
        final ParseConfig config = ParseConfig.getCurrentConfig();
        final List<String> currencyCodes = config.getList(ParseConfigUtils.SUPPORTED_CURRENCIES);
        final List<String> currencyNames = MoneyUtils.getCurrencyDisplayNames(currencyCodes);

        final int currencyNamesLength = currencyNames.size();
        final List<Currency> currencies =
                new ArrayList<>(currencyNamesLength);
        for (int i = 0; i < currencyNamesLength; i++) {
            currencies.add(new Currency(currencyNames.get(i),
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
