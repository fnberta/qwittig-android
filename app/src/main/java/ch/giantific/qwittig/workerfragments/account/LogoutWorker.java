/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.account;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import ch.giantific.qwittig.workerfragments.BaseWorker;
import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.domain.models.parse.Installation;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Resets the device's {@link ParseInstallation} object and logs out the current user.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class LogoutWorker extends BaseGoogleApiLoginWorker {

    private static final String BUNDLE_DELETE_USER = "BUNDLE_DELETE_USER";

    @Nullable
    private WorkerInteractionListener mListener;
    private boolean mDeleteUser;

    public LogoutWorker() {
        // empty default constructor
    }

    public static LogoutWorker newInstance(boolean deleteUser) {
        LogoutWorker fragment = new LogoutWorker();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_DELETE_USER, deleteUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WorkerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mDeleteUser = args.getBoolean(BUNDLE_DELETE_USER, false);
        }

        final User currentUser = (User) ParseUser.getCurrentUser();
        final boolean isGoogleUser = currentUser.isGoogleUser();
        if (isGoogleUser) {
            setupGoogleApiClient();
        } else if (mDeleteUser) {
            deleteUserInCloud();
        } else {
            handleInstallation();
        }
    }

    @Override
    protected void onGoogleClientConnected() {
        if (mDeleteUser) {
            unlinkGoogle();
        } else {
            signOutGoogle();
        }
    }

    @Override
    protected void onGoogleClientConnectionFailed() {
        if (mListener != null) {
            mListener.onLogoutFailed(0);
        }
    }

    @Override
    protected void onGoogleUnlinkSuccessful() {
        deleteUserInCloud();
    }

    @Override
    protected void onGoogleUnlinkFailed(int errorCode) {
        if (mListener != null) {
            mListener.onLogoutFailed(errorCode);
        }
    }

    private void signOutGoogle() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    handleInstallation();
                } else if (mListener != null) {
                    mListener.onLogoutFailed(status.getStatusCode());
                }
            }
        });
    }

    private void deleteUserInCloud() {
        CloudCodeClient cloudCode = new CloudCodeClient();
        cloudCode.deleteAccount(new CloudCodeClient.CloudCodeListener() {
            @Override
            public void onCloudFunctionReturned(Object result) {
                handleInstallation();
            }

            @Override
            public void onCloudFunctionFailed(int errorCode) {
                if (mListener != null) {
                    mListener.onLogoutFailed(errorCode);
                }
            }
        });
    }

    /**
     * Un-subscribes the device from all notification channels and removes the current user from the
     * installation object, so that the device does not keep receiving push notifications.
     */
    private void handleInstallation() {
        ParseInstallation installation = Installation.getResetInstallation();
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLogoutFailed(e.getCode());
                    }
                    return;
                }

                if (mDeleteUser) {
                    deleteUser();
                } else {
                    logOut();
                }
            }
        });
    }

    private void deleteUser() {
        final User currentUser = (User) ParseUser.getCurrentUser();
        final String username = currentUser.getUsername();
        currentUser.deleteUserFields();
        if (currentUser.isFacebookUser()) {
            ParseFacebookUtils.unlinkInBackground(currentUser, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    onDeleteUserSaved(currentUser, username, e);
                }
            });
        } else {
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(@Nullable ParseException e) {
                    onDeleteUserSaved(currentUser, username, e);
                }
            });
        }
    }

    private void onDeleteUserSaved(User currentUser, String username, ParseException e) {
        if (e != null) {
            currentUser.undeleteUserFields(username);
            if (mListener != null) {
                mListener.onLogoutFailed(e.getCode());
            }

            return;
        }

        logOut();
    }

    private void logOut() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                // ignore possible exception, currentUser will always be null now
                onLogoutSucceeded();
            }
        });
    }

    private void onLogoutSucceeded() {
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
    public interface WorkerInteractionListener {
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
