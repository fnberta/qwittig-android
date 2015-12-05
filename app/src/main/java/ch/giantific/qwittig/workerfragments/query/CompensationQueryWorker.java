/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.query;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.data.repositories.ParseCompensationRepository;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;

/**
 * Performs an online query to the Parse.com database to query either paid or unpaid compensations.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class CompensationQueryWorker extends BaseQueryWorker implements
        CompensationRepository.UpdateCompensationsListener {

    private static final String LOG_TAG = CompensationQueryWorker.class.getSimpleName();
    private static final String BUNDLE_QUERY_PAID = "BUNDLE_QUERY_PAID";
    private boolean mQueryPaid;
    @Nullable
    private WorkerInteractionListener mListener;

    public CompensationQueryWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link CompensationQueryWorker} with an argument whether to
     * query for paid or unpaid compensations.
     *
     * @param queryPaid whether to query paid compensations
     * @return a new instance of {@link CompensationQueryWorker}
     */
    @NonNull
    public static CompensationQueryWorker newInstance(boolean queryPaid) {
        CompensationQueryWorker fragment = new CompensationQueryWorker();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_QUERY_PAID, queryPaid);
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

        Bundle args = getArguments();
        if (args != null) {
            mQueryPaid = args.getBoolean(BUNDLE_QUERY_PAID, false);
        }

        if (setCurrentGroups()) {
            CompensationRepository repo = new ParseCompensationRepository(getActivity());
            if (mQueryPaid) {
                repo.updateCompensationsPaidAsync(mCurrentUserGroups, mCurrentGroup.getObjectId(), this);
            } else {
                repo.updateCompensationsUnpaidAsync(mCurrentUserGroups, this);
            }
        } else if (mListener != null) {
            if (mQueryPaid) {
                mListener.onAllCompensationsPaidUpdated();
            } else {
                mListener.onCompensationsUpdated(false);
            }
        }
    }

    @Override
    public void onCompensationsPaidUpdated(boolean isCurrentGroup) {
        if (isCurrentGroup && mListener != null) {
            mListener.onCompensationsUpdated(true);
        }
    }

    @Override
    public void onAllCompensationsPaidUpdated() {
        if (mListener != null) {
            mListener.onAllCompensationsPaidUpdated();
        }
    }

    @Override
    public void onCompensationsUnpaidUpdated() {
        if (mListener != null) {
            mListener.onCompensationsUpdated(false);
        }
    }

    @Override
    public void onCompensationUpdateFailed(@StringRes int errorMessage) {
        if (mListener != null) {
            mListener.onCompensationUpdateFailed(errorMessage, mQueryPaid);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after compensations are updated.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful update of local compensations.
         *
         * @param isPaid whether the compensations are paid or unpaid
         */
        void onCompensationsUpdated(boolean isPaid);

        /**
         * Handles the failed update of local compensations.
         *
         * @param errorMessage the error message from the exception thrown in the process
         * @param isPaid    whether the compensations are paid or unpaid
         */
        void onCompensationUpdateFailed(@StringRes int errorMessage, boolean isPaid);

        /**
         * Handles the successful update of all paid or unpaid compensations from all groups.
         */
        void onAllCompensationsPaidUpdated();
    }
}
