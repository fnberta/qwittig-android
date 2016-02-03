/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.annotation.NonNull;

/**
 * Provides useful static utility methods for the handling of retained headless worker fragments.
 */
public class WorkerUtils {

    private WorkerUtils() {
        // class cannot be instantiated
    }

    /**
     * Removes a worker fragment from the stack regardless of possible state loss.
     *
     * @param fragmentManager the fragment manager to use to remove the fragment
     * @param tag             the tag to find the fragment
     */
    public static void removeWorker(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        final Fragment fragment = findWorker(fragmentManager, tag);

        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    /**
     * Returns the worker fragment associated with the specified tag.
     *
     * @param fragmentManager the fragment manager to use to find the fragment
     * @param tag             the tag to find the fragment
     * @return the worker fragment associated with the tag
     */
    public static Fragment findWorker(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        return fragmentManager.findFragmentByTag(tag);
    }
}
