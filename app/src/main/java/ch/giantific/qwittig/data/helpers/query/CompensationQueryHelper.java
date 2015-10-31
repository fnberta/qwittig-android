/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.query;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.ParseCompensationRepository;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;

/**
 * Performs an online query to the Parse.com database to query either paid or unpaid compensations.
 * <p/>
 * Subclass of {@link BaseQueryHelper}.
 */
public class CompensationQueryHelper extends BaseQueryHelper implements
        CompensationRepository.UpdateCompensationsListener {

    private static final String LOG_TAG = CompensationQueryHelper.class.getSimpleName();
    private static final String BUNDLE_QUERY_PAID = "BUNDLE_QUERY_PAID";
    private boolean mQueryPaid;
    @Nullable
    private HelperInteractionListener mListener;

    public CompensationQueryHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link CompensationQueryHelper} with an argument whether to
     * query for paid or unpaid compensations.
     *
     * @param queryPaid whether to query paid compensations
     * @return a new instance of {@link CompensationQueryHelper}
     */
    @NonNull
    public static CompensationQueryHelper newInstance(boolean queryPaid) {
        CompensationQueryHelper fragment = new CompensationQueryHelper();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_QUERY_PAID, queryPaid);
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

        Bundle args = getArguments();
        if (args != null) {
            mQueryPaid = args.getBoolean(BUNDLE_QUERY_PAID, false);
        }

        if (setCurrentGroups()) {
            CompensationRepository repo = new ParseCompensationRepository();
            if (mQueryPaid) {
                repo.updateCompensationsPaidAsync(mCurrentUserGroups, mCurrentGroup.getObjectId(), this);
            } else {
                repo.updateCompensationsUnpaidAsync(mCurrentUserGroups, this);
            }
        } else {
            if (mListener != null) {
                if (mQueryPaid) {
                    mListener.onAllCompensationsPaidUpdated();
                } else {
                    mListener.onCompensationsUpdated(false);
                }
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
    public void onCompensationUpdateFailed(int errorCode) {
        if (mListener != null) {
            mListener.onCompensationUpdateFailed(errorCode, mQueryPaid);
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
    public interface HelperInteractionListener {
        /**
         * Handles the successful update of local compensations.
         *
         * @param isPaid whether the compensations are paid or unpaid
         */
        void onCompensationsUpdated(boolean isPaid);

        /**
         * Handles the failed update of local compensations.
         *
         * @param errorCode the error code of the exception thrown in the process
         * @param isPaid    whether the compensations are paid or unpaid
         */
        void onCompensationUpdateFailed(int errorCode, boolean isPaid);

        /**
         * Handles the successful update of all paid or unpaid compensations from all groups.
         */
        void onAllCompensationsPaidUpdated();
    }
}
