/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseACL;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Provides useful static utility methods related to the Parse.com framework.
 */
public class ParseUtils {

    private ParseUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns the currency code for the current group or the systems default if the current group
     * is null.
     *
     * @return the currency code for the current group
     */
    public static String getGroupCurrency() {
        Currency fallback = Currency.getInstance(Locale.getDefault());
        String currencyCode = fallback.getCurrencyCode();

        Group currentGroup = getCurrentGroup();
        if (currentGroup != null) {
            return currentGroup.getCurrency();
        }

        return currencyCode;
    }

    /**
     * Returns the current group of the current user or null if either of them is null.
     *
     * @return the current group of the current user
     */
    @Nullable
    public static Group getCurrentGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        Group currentGroup = null;
        if (currentUser != null) {
            currentGroup = currentUser.getCurrentGroup();
        }
        return currentGroup;
    }

    /**
     * Returns the groups of the current user or an empty list if the current user is null.
     *
     * @return the groups of the current user
     */
    @NonNull
    public static List<ParseObject> getCurrentUserGroups() {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser == null) {
            return Collections.emptyList();
        }

        return currentUser.getGroups();
    }

    /**
     * Returns whether the passed in user is a test user or a valid one.
     *
     * @param parseUser the user to test
     * @return whether the passed in user is a test user
     */
    public static boolean isTestUser(@NonNull ParseUser parseUser) {
        User user = (User) parseUser;
        String username = user.getUsername();
        return username.startsWith(User.USERNAME_PREFIX_TEST);
    }

    /**
     * Returns a default {@link ParseACL} with read/write access for the role of the passed group.
     *
     * @param group the group to get the role from
     * @return a default {@link ParseACL}
     */
    @NonNull
    public static ParseACL getDefaultAcl(@NonNull ParseObject group) {
        String roleName = getGroupRoleName(group);
        ParseACL acl = new ParseACL();
        acl.setRoleReadAccess(roleName, true);
        acl.setRoleWriteAccess(roleName, true);
        return acl;
    }

    @NonNull
    private static String getGroupRoleName(@NonNull ParseObject group) {
        return Group.ROLE_PREFIX + group.getObjectId();
    }

    /**
     * Returns the currently supported currencies as
     * {@link ch.giantific.qwittig.data.models.Currency} objects with a name and currency code.
     * Reads the information from {@link ParseConfig}.
     *
     * @return the currently supported currencies
     */
    @NonNull
    public static List<ch.giantific.qwittig.data.models.Currency> getSupportedCurrencies() {
        ParseConfig config = ParseConfig.getCurrentConfig();
        List<String> currencyCodes = config.getList(Config.SUPPORTED_CURRENCIES);
        List<String> currencyNames = MoneyUtils.getCurrencyDisplayNames(currencyCodes);

        int currencyNamesLength = currencyNames.size();
        List<ch.giantific.qwittig.data.models.Currency> currencies =
                new ArrayList<>(currencyNamesLength);
        for (int i = 0; i < currencyNamesLength; i++) {
            currencies.add(new ch.giantific.qwittig.data.models.Currency(currencyNames.get(i),
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
        ParseConfig config = ParseConfig.getCurrentConfig();

        return config.getList(Config.SUPPORTED_CURRENCIES);
    }

    /**
     * Returns a no connection {@link ParseException} with an empty error message.
     *
     * @return a no connection {@link ParseException}
     */
    @NonNull
    public static ParseException getNoConnectionException() {
        return getNoConnectionException("");
    }

    /**
     * Returns a no connection {@link ParseException} with an error message.
     *
     * @return a no connection {@link ParseException}
     */
    @NonNull
    public static ParseException getNoConnectionException(String message) {
        return new ParseException(ParseException.CONNECTION_FAILED, message);
    }
}
