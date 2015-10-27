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
 * Performs an online query to the Parse.com database to query tasks.
 * <p/>
 * Subclass of {@link BaseQueryHelper}.
 */
public class TaskQueryHelper extends BaseQueryHelper {

    private static final String LOG_TAG = TaskQueryHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;

    public TaskQueryHelper() {
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
                mListener.onAllTasksQueried();
            }
            return;
        }

        mTotalNumberOfQueries = 1;
        queryTasks();
    }

    @Override
    protected void onParseError(ParseException e) {
        if (mListener != null) {
            mListener.onTasksPinFailed(e);
        }
    }

    @Override
    protected void finish() {
        if (mListener != null) {
            mListener.onAllTasksQueried();
        }
    }

    @Override
    void onTasksPinned() {
        super.onTasksPinned();

        if (mListener != null) {
            mListener.onTasksPinned();
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
        void onTasksPinned();

        /**
         * Handles the failed query or pin to the local data store.
         *
         * @param e the {@link ParseException} thrown in the process
         */
        void onTasksPinFailed(@NonNull ParseException e);

        /**
         * Handles the completion of all queries.
         */
        void onAllTasksQueried();
    }
}
