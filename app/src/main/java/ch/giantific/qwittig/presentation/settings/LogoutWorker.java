/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.common.workers.BaseGoogleApiLoginWorker;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import ch.giantific.qwittig.utils.parse.ParseInstallationUtils;

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
            mListener.onLogoutFailed(R.string.toast_no_connection);
        }
    }

    @Override
    protected void onGoogleUnlinkSuccessful() {
        deleteUserInCloud();
    }

    @Override
    protected void onGoogleUnlinkFailed() {
        if (mListener != null) {
            mListener.onLogoutFailed(R.string.toast_unknown_error);
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
//        ApiRepository cloudCode = new ApiRepository(getActivity());
//        cloudCode.deleteAccount(new ApiRepository.CloudCodeListener() {
//            @Override
//            public void onCloudFunctionReturned(Object result) {
//                handleInstallation();
//            }
//
//            @Override
//            public void onCloudFunctionFailed(@StringRes int errorMessage) {
//                if (mListener != null) {
//                    mListener.onLogoutFailed(errorMessage);
//                }
//            }
//        });
    }

    /**
     * Un-subscribes the device from all notification channels and removes the current user from the
     * installation object, so that the device does not keep receiving push notifications.
     */
    private void handleInstallation() {
        ParseInstallation installation = ParseInstallationUtils.getResetInstallation();
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLogoutFailed(ParseErrorHandler.handleParseError(getActivity(), e));
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
//        final User currentUser = (User) ParseUser.getCurrentUser();
//        final String username = currentUser.getUsername();
//        currentUser.deleteUserFields();
//        if (currentUser.isFacebookUser()) {
//            ParseFacebookUtils.unlinkInBackground(currentUser, new SaveCallback() {
//                @Override
//                public void done(ParseException e) {
//                    onDeleteUserSaved(currentUser, username, e);
//                }
//            });
//        } else {
//            currentUser.saveInBackground(new SaveCallback() {
//                @Override
//                public void done(@Nullable ParseException e) {
//                    onDeleteUserSaved(currentUser, username, e);
//                }
//            });
//        }
    }

//    private void onDeleteUserSaved(User currentUser, String username, ParseException e) {
//        if (e != null) {
//            currentUser.undeleteUserFields(username);
//            if (mListener != null) {
//                mListener.onLogoutFailed(ParseErrorHandler.handleParseError(getActivity(), e));
//            }
//
//            return;
//        }
//
//        logOut();
//    }

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
         * @param errorMessage the error message from the exception thrown during the process
         */
        void onLogoutFailed(@StringRes int errorMessage);

        /**
         * Handles a successful logout of the user.
         */
        void onLoggedOut();
    }
}
