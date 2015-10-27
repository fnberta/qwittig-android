/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.parse.ParseException;

/**
 * Performs online queries to Parse.com database to download all information related to the current
 * user.
 * <p/>
 * Every query starts only after the previous query finished successfully.
 * <p/>
 * Subclass of {@link BaseQueryHelper}.
 */
public class FullQueryHelper extends BaseQueryHelper {

    private static final String LOG_TAG = FullQueryHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;

    public FullQueryHelper() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mCurrentGroup == null || mCurrentUserGroups == null) {
            if (mListener != null) {
                mListener.onFullQueryFinished(true);
            }
            return;
        }

        // purchases for each group + compensationsPaid for each group + compensationsUnpaid + users + tasks
        mTotalNumberOfQueries = mCurrentUserGroups.size() * 2 + 1 + 1 + 1;
        calculateBalances();
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onPinFailed(e);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onFullQueryFinished(false);
        }
    }

    @Override
    void onBalancesCalculated() {
        super.onBalancesCalculated();

        queryUsers();
    }

    @Override
    void onUsersPinned() {
        super.onUsersPinned();

        if (mListener != null) {
            mListener.onUsersPinned();
        }

        if (!checkQueryCount()) {
            queryPurchases();
        }
    }

    @Override
    void onPurchasesPinned(@NonNull String groupId) {
        super.onPurchasesPinned(groupId);

        if (mListener != null && mCurrentGroup != null &&
                groupId.equals(mCurrentGroup.getObjectId())) {
            mListener.onPurchasesPinned();
        }

        if (!checkQueryCount()) {
            queryTasks();
        }
    }

    @Override
    void onTasksPinned() {
        super.onTasksPinned();

        if (mListener != null) {
            mListener.onTasksPinned();
        }

        if (!checkQueryCount()) {
            queryCompensations();
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
     * Defines the action to be taken after the different queries either finish or fail.
     */
    public interface HelperInteractionListener {
        /**
         * Handles the successful pin of the users to the local data store.
         */
        void onUsersPinned();

        /**
         * Handles the successful pin of the purchases to the local data store.
         */
        void onPurchasesPinned();

        /**
         *  Handles the successful pin of the compensations to the local data store.
         */
        void onCompensationsPinned(boolean isPaid);

        /**
         * Handles the successful pin of the tasks to the local data store.
         */
        void onTasksPinned();

        /**
         * Handles the case when all queries are finished.
         *
         * @param failedEarly whether the process failed early because the current user or current
         *                    group were null or it finished as planned
         */
        void onFullQueryFinished(boolean failedEarly);

        /**
         * Handles the failure of one of the attempts to pin to the local data store.
         *
         * @param e the {@link ParseException} thrown during the process
         */
        void onPinFailed(@NonNull ParseException e);
    }
}
