/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.query;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Performs an online query to the Parse.com database to query users.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class UserQueryWorker extends BaseQueryWorker implements
        CloudCodeClient.CloudCodeListener,
        UserRepository.UpdateUsersListener {

    private static final String LOG_TAG = UserQueryWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;

    public UserQueryWorker() {
        // empty default constructor
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

        if (setCurrentGroups()) {
            CloudCodeClient cloudCode = new CloudCodeClient();
            cloudCode.calcUserBalances(this);
        } else {
            if (mListener != null) {
                mListener.onUsersUpdated();
            }
        }
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        UserRepository userRepo = new ParseUserRepository();
        userRepo.updateUsersAsync(mCurrentUserGroups, this);
    }

    @Override
    public void onCloudFunctionFailed(int errorCode) {
        if (mListener != null) {
            mListener.onUserUpdateFailed(errorCode);
        }
    }

    @Override
    public void onUsersUpdated() {
        if (mListener != null) {
            mListener.onUsersUpdated();
        }
    }

    @Override
    public void onUserUpdateFailed(int errorCode) {
        if (mListener != null) {
            mListener.onUserUpdateFailed(errorCode);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after users are updated.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful update of local users including the re-calcuation of their
         * balances.
         */
        void onUsersUpdated();

        /**
         * Handles the failed update of local users.
         *
         * @param errorCode the error code of the exception thrown in the process
         */
        void onUserUpdateFailed(int errorCode);
    }
}
