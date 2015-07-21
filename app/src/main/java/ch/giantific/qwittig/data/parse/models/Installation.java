package ch.giantific.qwittig.data.parse.models;

import com.parse.ParseInstallation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabio on 15.03.15.
 */
public class Installation {

    private Installation() {
        // class cannot be instantiated
    }

    public static final String USER = "user";
    public static final String CHANNELS = "channels";

    public static ParseInstallation getResetInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        List<String> defaultChannels = new ArrayList<>();
        defaultChannels.add("");
        installation.put(Installation.CHANNELS, defaultChannels);
        installation.remove(Installation.USER);

        return installation;
    }
}
