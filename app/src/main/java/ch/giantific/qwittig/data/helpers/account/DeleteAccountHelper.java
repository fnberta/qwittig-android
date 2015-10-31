/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.account;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.rest.CloudCodeClient;

/**
 * Deletes a user's account and if successful logs him/she out.
 * <p/>
 * Subclass of {@link LogoutHelper}.
 */
public class DeleteAccountHelper extends LogoutHelper implements
        CloudCodeClient.CloudCodeListener {

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
        CloudCodeClient cloudCode = new CloudCodeClient();
        cloudCode.deleteAccount(this);
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        handleInstallation();
    }

    @Override
    public void onCloudFunctionFailed(int errorCode) {
        if (mListener != null) {
            mListener.onDeleteUserFailed(errorCode);
        }
    }

    @Override
    protected void onInstallationSaveFailed(@NonNull ParseException e) {
        if (mListener != null) {
            mListener.onDeleteUserFailed(e.getCode());
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
                        mListener.onDeleteUserFailed(e.getCode());
                    }
                    return;
                }

                logOut();
            }
        });
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
         * @param errorCode the error code of the exception thrown during the process
         */
        void onDeleteUserFailed(int errorCode);
    }
}
