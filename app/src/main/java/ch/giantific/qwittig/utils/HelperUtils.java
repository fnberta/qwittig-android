/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.NonNull;

/**
 * Provides useful static utility methods for the handling of retained headlines helper fragments.
 */
public class HelperUtils {

    private HelperUtils() {
        // class cannot be instantiated
    }

    /**
     * Removes a helper fragment from the stack regardless of possible state loss.
     *
     * @param fragmentManager the fragment manager to use to remove the fragment
     * @param tag             the tag to find the fragment
     */
    public static void removeHelper(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        Fragment fragment = findHelper(fragmentManager, tag);

        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    /**
     * Returns the helper fragment associated with the specified tag.
     *
     * @param fragmentManager the fragment manager to use to find the fragment
     * @param tag             the tag to find the fragment
     * @return the helper fragment associated with the tag
     */
    public static Fragment findHelper(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        return fragmentManager.findFragmentByTag(tag);
    }
}
