/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.reminder;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.workerfragments.BaseWorker;

/**
 * Calls Parse.com cloud functions to remind a user that he/she should finish a task.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class TaskRemindWorker extends BaseWorker implements
        CloudCodeClient.CloudCodeListener {

    private static final String BUNDLE_TASK_ID = "BUNDLE_TASK_ID";
    private static final String LOG_TAG = TaskRemindWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;
    private String mTaskId;

    public TaskRemindWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link TaskRemindWorker} with an argument.
     *
     * @param taskId the object id of the task that should be finished
     * @return a new instance of {@link TaskRemindWorker}
     */
    @NonNull
    public static TaskRemindWorker newInstance(@NonNull String taskId) {
        TaskRemindWorker fragment = new TaskRemindWorker();
        Bundle args = new Bundle();
        args.putString(BUNDLE_TASK_ID, taskId);
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
            mTaskId = args.getString(BUNDLE_TASK_ID, "");
        }

        if (TextUtils.isEmpty(mTaskId)) {
            if (mListener != null) {
                mListener.onUserRemindFailed(mTaskId, 0);
            }

            return;
        }

        CloudCodeClient cloudCode = new CloudCodeClient();
        cloudCode.pushTaskReminder(mTaskId, this);
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        if (mListener != null) {
            mListener.onUserReminded(mTaskId);
        }
    }

    @Override
    public void onCloudFunctionFailed(int errorCode) {
        if (mListener != null) {
            mListener.onUserRemindFailed(mTaskId, errorCode);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the actions to take after a user was reminded or after the process failed.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful reminder of a user to finish a task.
         *
         * @param taskId the object id of the task to be finished
         */
        void onUserReminded(@NonNull String taskId);

        /**
         * Handles the failed reminder of a user to finish a task.
         *
         * @param taskId    the object id of the task to be finished
         * @param errorCode the error code of the exception thrown during the process
         */
        void onUserRemindFailed(@NonNull String taskId, int errorCode);
    }
}
