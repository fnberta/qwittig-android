/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils.parse;

import com.parse.ParseInstallation;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides static references to values used in {@link ParseInstallation} entries.
 */
public class ParseInstallationUtils {

    public static final String USER = "user";
    public static final String CHANNELS = "channels";
    private ParseInstallationUtils() {
        // class cannot be instantiated
    }

    /**
     * Returns a {@link ParseInstallation} with empty channels and no user field.
     *
     * @return the reset installation object
     */
    public static ParseInstallation getResetInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        List<String> defaultChannels = new ArrayList<>();
        defaultChannels.add("");
        installation.put(ParseInstallationUtils.CHANNELS, defaultChannels);
        installation.remove(ParseInstallationUtils.USER);

        return installation;
    }
}
