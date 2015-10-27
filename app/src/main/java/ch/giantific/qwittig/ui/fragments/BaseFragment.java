/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;

/**
 * Provides an abstract base class for fragments to house commonly used methods.
 * <p/>
 * Currently only incorporates a base interface for communication with an activity.
 * <p/>
 * Subclass of {@link Fragment}.
 */
public abstract class BaseFragment extends Fragment {

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface BaseFragmentInteractionListener {
        /**
         * Handles the request to show the create account dialog.
         */
        void showAccountCreateDialog();
    }
}
