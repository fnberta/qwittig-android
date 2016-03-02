/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

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
     * @param fragmentManager the fragment manager to use for the transaction
     * @param tag             the tag to find the worker fragment
     */
    public static void removeWorker(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        final Fragment worker = fragmentManager.findFragmentByTag(tag);
        if (worker != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(worker)
                    .commitAllowingStateLoss();
        }
    }
}
