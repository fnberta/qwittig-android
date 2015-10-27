/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseException;
import com.parse.ParseObject;

/**
 * Performs an online query to the Parse.com database to query either paid or unpaid compensations.
 * <p/>
 * Subclass of {@link BaseQueryHelper}.
 */
public class CompensationQueryHelper extends BaseQueryHelper {

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

        if (mCurrentGroup == null || mCurrentUserGroups == null) {
            mListener.onAllCompensationsQueried(mQueryPaid);
            return;
        }

        if (mQueryPaid) {
            mTotalNumberOfQueries = mCurrentUserGroups.size();
            for (ParseObject group : mCurrentUserGroups) {
                queryCompensationsPaid(group);
            }
        } else {
            mTotalNumberOfQueries = 1;
            queryCompensationsUnpaid(mCurrentUserGroups);
        }
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onCompensationsPinFailed(e, mQueryPaid);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onAllCompensationsQueried(mQueryPaid);
        }
    }

    @Override
    void onCompensationsUnpaidPinned() {
        super.onCompensationsUnpaidPinned();

        if (mListener != null) {
            mListener.onCompensationsPinned(false);
        }

        checkQueryCount();
    }

    @Override
    void onCompensationsPaidPinned(@NonNull String groupId) {
        super.onCompensationsPaidPinned(groupId);

        if (mListener != null && mCurrentGroup != null &&
                groupId.equals(mCurrentGroup.getObjectId())) {
            mListener.onCompensationsPinned(true);
        }

        checkQueryCount();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after a query finished.
     */
    public interface HelperInteractionListener {
        /**
         * Handles the successful query and pin to the local data store.
         *
         * @param isPaid whether the compensations are paid or unpaid
         */
        void onCompensationsPinned(boolean isPaid);

        /**
         * Handles the failed query or pin.
         *
         * @param e      the {@link ParseException} thrown in the process
         * @param isPaid whether the compensations are paid or unpaid
         */
        void onCompensationsPinFailed(@NonNull ParseException e, boolean isPaid);

        /**
         * Handles the completion of all queries.
         *
         * @param isPaid whether the compensations are paid or unpaid
         */
        void onAllCompensationsQueried(boolean isPaid);
    }
}
