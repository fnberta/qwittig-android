/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.account;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import ch.giantific.qwittig.data.helpers.BaseHelper;
import ch.giantific.qwittig.domain.models.parse.Installation;

/**
 * Resets the device's {@link ParseInstallation} object and logs out the current user.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class LogoutHelper extends BaseHelper {

    @Nullable
    private HelperInteractionListener mListener;

    public LogoutHelper() {
        // empty default constructor
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startProcess();
    }

    void startProcess() {
        handleInstallation();
    }

    /**
     * Un-subscribes the device from all notification channels and removes the current user from the
     * installation object, so that the device does not keep receiving push notifications.
     */
    final void handleInstallation() {
        ParseInstallation installation = Installation.getResetInstallation();
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    onInstallationSaveFailed(e);
                    return;
                }

                onInstallationSaved();
            }
        });
    }

    void onInstallationSaveFailed(@NonNull ParseException e) {
        if (mListener != null) {
            mListener.onLogoutFailed(e.getCode());
        }
    }

    void onInstallationSaved() {
        logOut();
    }

    final void logOut() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                // ignore possible exception, currentUser will always be null now
                onLogoutSucceeded();
            }
        });
    }

    void onLogoutSucceeded() {
        if (mListener != null) {
            mListener.onLoggedOut();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after the user was logged out or the logout failed
     */
    public interface HelperInteractionListener {
        /**
         * Handles the failed logout of a user.
         *
         * @param errorCode the error code of the exception thrown during the process
         */
        void onLogoutFailed(int errorCode);

        /**
         * Handles a successful logout of the user.
         */
        void onLoggedOut();
    }
}
