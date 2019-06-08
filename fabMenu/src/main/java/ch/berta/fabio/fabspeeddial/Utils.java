package ch.berta.fabio.fabspeeddial;

import android.os.Build;

/**
 * Provides useful static utility methods.
 */
public class Utils {

    private Utils() {
        // class cannot be instantiated
    }

    /**
     * Returns whether the device is running Android Lollipop or higher.
     *
     * @return whether the device is running Android Lollipop or higher
     */
    public static boolean isRunningLollipopAndHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
