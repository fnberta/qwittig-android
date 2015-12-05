/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.parse.ParseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.workerfragments.BaseWorker;
import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Creates and saves a new user settlement by calling Parse.com cloud functions.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class SettlementWorker extends BaseWorker implements
        CloudCodeClient.CloudCodeListener {

    private static final String BUNDLE_SINGLE_USER = "BUNDLE_SINGLE_USER";
    private static final String LOG_TAG = SettlementWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;

    public SettlementWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link SettlementWorker}.
     *
     * @param doSingleUserSettlement whether the settlement should be calculated only for the
     *                               current user or for all users of the current group
     * @return a new instance of {@link SettlementWorker}
     */
    @NonNull
    public static SettlementWorker newInstance(boolean doSingleUserSettlement) {
        SettlementWorker fragment = new SettlementWorker();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_SINGLE_USER, doSingleUserSettlement);
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

        boolean doSingleUserSettlement = false;
        Bundle args = getArguments();
        if (args != null) {
            doSingleUserSettlement = args.getBoolean(BUNDLE_SINGLE_USER, false);
        }

        final User currentUser = (User) ParseUser.getCurrentUser();
        final Group currentGroup = currentUser.getCurrentGroup();
        if (currentGroup == null) {
            if (mListener != null) {
                mListener.onNewSettlementCreationFailed(R.string.toast_unknown_error);
            }

            return;
        }

        CloudCodeClient cloudCode = new CloudCodeClient(getActivity());
        cloudCode.startNewSettlement(currentGroup.getObjectId(), doSingleUserSettlement, this);
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        if (mListener != null) {
            mListener.onNewSettlementCreated();
        }
    }

    @Override
    public void onCloudFunctionFailed(@StringRes int errorMessage) {
        if (mListener != null) {
            mListener.onNewSettlementCreationFailed(errorMessage);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after a new settlement was calculated or after the calculation
     * failed.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful calculation of a new settlement.
         */
        void onNewSettlementCreated();

        /**
         * Handles the failed calculation of a new settlement.
         *
         * @param errorMessage the error message from the exception thrown during the process
         */
        void onNewSettlementCreationFailed(@StringRes int errorMessage);
    }
}
