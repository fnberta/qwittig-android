/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.helpers.BaseHelper;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * Creates and saves a new user settlement by calling Parse.com cloud functions.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class SettlementHelper extends BaseHelper implements
        CloudCodeClient.CloudCodeListener {

    private static final String BUNDLE_SINGLE_USER = "BUNDLE_SINGLE_USER";
    private static final String LOG_TAG = SettlementHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;

    public SettlementHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link SettlementHelper}.
     *
     * @param doSingleUserSettlement whether the settlement should be calculated only for the
     *                               current user or for all users of the current group
     * @return a new instance of {@link SettlementHelper}
     */
    @NonNull
    public static SettlementHelper newInstance(boolean doSingleUserSettlement) {
        SettlementHelper fragment = new SettlementHelper();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_SINGLE_USER, doSingleUserSettlement);
        fragment.setArguments(args);
        return fragment;
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

        boolean doSingleUserSettlement = false;
        Bundle args = getArguments();
        if (args != null) {
            doSingleUserSettlement = args.getBoolean(BUNDLE_SINGLE_USER, false);
        }

        Group currentGroup = ParseUtils.getCurrentGroup();
        if (currentGroup == null) {
            if (mListener != null) {
                mListener.onNewSettlementCreationFailed(0);
            }

            return;
        }

        CloudCodeClient cloudCode = new CloudCodeClient();
        cloudCode.startNewSettlement(currentGroup.getObjectId(), doSingleUserSettlement, this);
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        if (mListener != null) {
            mListener.onNewSettlementCreated();
        }
    }

    @Override
    public void onCloudFunctionFailed(int errorCode) {
        if (mListener != null) {
            mListener.onNewSettlementCreationFailed(errorCode);
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
    public interface HelperInteractionListener {
        /**
         * Handles the successful calculation of a new settlement.
         */
        void onNewSettlementCreated();

        /**
         * Handles the failed calculation of a new settlement.
         *
         * @param errorCode the error code of the exception thrown during the process
         */
        void onNewSettlementCreationFailed(int errorCode);
    }
}
