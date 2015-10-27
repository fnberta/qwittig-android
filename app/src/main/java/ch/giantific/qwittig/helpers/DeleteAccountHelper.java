/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Deletes a user's account and if successful logs him/she out.
 * <p/>
 * Subclass of {@link LogoutHelper}.
 */
public class DeleteAccountHelper extends LogoutHelper {

    @Nullable
    private HelperInteractionListener mListener;

    public DeleteAccountHelper() {
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
    void startProcess() {
        deleteAccount();
    }

    private void deleteAccount() {
        Map<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground(CloudCode.DELETE_ACCOUNT, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onDeleteUserFailed(e);
                    }
                    return;
                }

                handleInstallation();
            }
        });
    }

    @Override
    protected void onInstallationSaveFailed(@NonNull ParseException e) {
        if (mListener != null) {
            mListener.onDeleteUserFailed(e);
        }
    }

    @Override
    protected void onInstallationSaved() {
        deleteUser();
    }

    private void deleteUser() {
        final User currentUser = (User) ParseUser.getCurrentUser();
        final String username = currentUser.getUsername();
        currentUser.deleteUserFields();
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    currentUser.undeleteUserFields(username);

                    if (mListener != null) {
                        mListener.onDeleteUserFailed(e);
                    }
                    return;
                }

                logOut();
            }
        });

        // TODO: check in CloudCode if user is the only member in his groups, if yes delete groups (tricky!!)
    }

    @Override
    protected void onLogoutSucceeded() {
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
     * Defines the actions to take after the deletion of a user's account failed.
     * <p/>
     * Extends {@link LogoutHelper.HelperInteractionListener}.
     */
    public interface HelperInteractionListener extends LogoutHelper.HelperInteractionListener {
        /**
         * Handles the failed deletion of a user's account.
         *
         * @param e the {@link ParseException} thrown during the process
         */
        void onDeleteUserFailed(@NonNull ParseException e);
    }
}
