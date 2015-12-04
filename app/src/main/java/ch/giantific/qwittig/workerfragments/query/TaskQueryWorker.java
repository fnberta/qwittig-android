/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.query;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
            TaskRepository repo = new ParseTaskRepository();
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
    public void onTaskUpdateFailed(int errorCode) {
        if (mListener != null) {
            mListener.onTasksUpdatedFailed(errorCode);
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
         * @param errorCode the error code of the exception thrown in the process
         */
        void onTasksUpdatedFailed(int errorCode);
    }
}
