/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.models.parse;

import com.parse.ParseConfig;

/**
 * Provides static references to the {@link ParseConfig} values used in this project.
 */
public class Config {

    public static final String TEST_USERS_PASSWORD = "testUsersPassword";
    public static final String TEST_USERS_NICKNAMES = "testUsersNicknames";
    public static final String SUPPORTED_CURRENCIES = "supportedCurrencies";
    public static final String DEFAULT_STORES = "defaultStores";
    public static final String FREE_PURCHASES_LIMIT = "freePurchasesLimit";

    private static final long CONFIG_REFRESH_INTERVAL = 12 * 60 * 60 * 1000;
    private static long LAST_FETCHED_TIME;

    private Config() {
        // class cannot be instantiated
    }

    /**
     * Fetches the {@link ParseConfig} at most once every 12 hours per app runtime.
     */
    public static void refreshConfig() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - LAST_FETCHED_TIME > CONFIG_REFRESH_INTERVAL) {
            LAST_FETCHED_TIME = currentTime;
            ParseConfig.getInBackground();
        }
    }
}
