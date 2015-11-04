/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;

import com.parse.ParseUser;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Provides an abstract base class for fragments to house commonly used methods.
 * <p/>
 * Currently only incorporates a base interface for communication with an activity.
 * <p/>
 * Subclass of {@link Fragment}.
 */
public abstract class BaseFragment extends Fragment {

    User mCurrentUser;
    Group mCurrentGroup;

    final void updateCurrentUserAndGroup() {
        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentGroup();
        }
    }

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
