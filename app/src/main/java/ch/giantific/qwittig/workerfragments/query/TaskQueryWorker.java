/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.query;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.data.repositories.ParseTaskRepository;
import ch.giantific.qwittig.domain.repositories.TaskRepository;

/**
 * Performs an online query to the Parse.com database to query tasks.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class TaskQueryWorker extends BaseQueryWorker implements
        TaskRepository.UpdateTasksListener {

    private static final String LOG_TAG = TaskQueryWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;

    public TaskQueryWorker() {
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
            TaskRepository repo = new ParseTaskRepository(getActivity());
            repo.updateTasksAsync(mCurrentUserGroups, this);
        } else {
            if (mListener != null) {
                mListener.onTasksUpdated();
            }
        }
    }

    @Override
    public void onTasksUpdated() {
        if (mListener != null) {
            mListener.onTasksUpdated();
        }
    }

    @Override
    public void onTaskUpdateFailed(@StringRes int errorMessage) {
        if (mListener != null) {
            mListener.onTasksUpdatedFailed(errorMessage);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after tasks are updated.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful update of local tasks.
         */
        void onTasksUpdated();

        /**
         * Handles the failed update of local tasks.
         *
         * @param errorMessage the error message from the exception thrown in the process
         */
        void onTasksUpdatedFailed(@StringRes int errorMessage);
    }
}
