/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils.parse;

import com.parse.ConfigCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;

/**
 * Provides static references to the {@link ParseConfig} values used in this project.
 */
public class ParseConfigUtils {

    public static final String TEST_USERS_PASSWORD = "testUsersPassword";
    public static final String TEST_USERS_NICKNAMES = "testUsersNicknames";
    public static final String SUPPORTED_CURRENCIES = "supportedCurrencies";
    public static final String DEFAULT_STORES = "defaultStores";
    public static final String FREE_PURCHASES_LIMIT = "freePurchasesLimit";

    private static final long CONFIG_REFRESH_INTERVAL = 12 * 60 * 60 * 1000;
    private static long sLastFetchedTime;

    private ParseConfigUtils() {
        // class cannot be instantiated
    }

    /**
     * Fetches the {@link ParseConfig} at most once every 12 hours per app runtime.
     */
    public static void refreshConfig() {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - sLastFetchedTime > CONFIG_REFRESH_INTERVAL) {
            ParseConfig.getInBackground(new ConfigCallback() {
                @Override
                public void done(ParseConfig config, ParseException e) {
                    if (e == null) {
                        sLastFetchedTime = currentTime;
                    } else {
                        sLastFetchedTime = 0;
                    }
                }
            });
        }
    }
}
