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
 * Performs an online query to the Parse.com database to query users.
 * <p/>
 * Subclass of {@link BaseQueryHelper}.
 */
public class UserQueryHelper extends BaseQueryHelper {

    private static final String LOG_TAG = UserQueryHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;

    public UserQueryHelper() {
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
                mListener.onAllUsersQueried();
            }
            return;
        }

        mTotalNumberOfQueries = 1;
        calculateBalances();
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onUsersPinFailed(e);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onAllUsersQueried();
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
         */
        void onUsersPinned();

        /**
         * Handles the failed query or pin to the local data store.
         *
         * @param e the {@link ParseException} thrown in the process
         */
        void onUsersPinFailed(@NonNull ParseException e);

        /**
         * Handles the completion of all queries.
         */
        void onAllUsersQueried();
    }
}
