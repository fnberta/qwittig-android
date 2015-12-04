/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.query;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;

/**
 * Performs an online query to the Parse.com database to query purchases.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class PurchaseQueryWorker extends BaseQueryWorker implements
        PurchaseRepository.UpdatePurchasesListener {

    private static final String LOG_TAG = PurchaseQueryWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;

    public PurchaseQueryWorker() {
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
            PurchaseRepository repo = new ParsePurchaseRepository();
            repo.updatePurchasesAsync(mCurrentUser, mCurrentUserGroups, mCurrentGroup.getObjectId(), this);
        } else {
            if (mListener != null) {
                mListener.onAllPurchasesUpdated();
            }
        }
    }

    @Override
    public void onPurchasesUpdated(boolean isCurrentGroup) {
        if (isCurrentGroup && mListener != null) {
            mListener.onPurchasesUpdated();
        }
    }

    @Override
    public void onPurchaseUpdateFailed(int errorCode) {
        if (mListener != null) {
            mListener.onPurchaseUpdateFailed(errorCode);
        }
    }

    @Override
    public void onAllPurchasesUpdated() {
        if (mListener != null) {
            mListener.onAllPurchasesUpdated();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after purchases are updated.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful update of local purchases.
         */
        void onPurchasesUpdated();

        /**
         * Handles the failed update of local purchases.
         *
         * @param errorCode the error code of the exception thrown in the process
         */
        void onPurchaseUpdateFailed(int errorCode);

        /**
         * Handles the successful update of all purchases from all groups.
         */
        void onAllPurchasesUpdated();
    }
}
