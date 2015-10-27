/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;

/**
 * Calls Parse.com cloud functions to remind a user that he/she should finish a task.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class TaskRemindHelper extends BaseHelper {

    private static final String BUNDLE_TASK_ID = "BUNDLE_TASK_ID";
    private static final String LOG_TAG = TaskRemindHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;

    public TaskRemindHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link TaskRemindHelper} with an argument.
     *
     * @param taskId the object id of the task that should be finished
     * @return a new instance of {@link TaskRemindHelper}
     */
    @NonNull
    public static TaskRemindHelper newInstance(@NonNull String taskId) {
        TaskRemindHelper fragment = new TaskRemindHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_TASK_ID, taskId);
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

        String taskId = "";
        Bundle args = getArguments();
        if (args != null) {
            taskId = args.getString(BUNDLE_TASK_ID);
        }

        if (!TextUtils.isEmpty(taskId)) {
            pushTaskRemind(taskId);
        }
    }

    private void pushTaskRemind(@NonNull final String taskId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_TASK, taskId);
        ParseCloud.callFunctionInBackground(CloudCode.PUSH_TASK_REMIND, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, @Nullable ParseException e) {
                if (e != null) {
                    onParseError(e, taskId);
                    return;
                }

                if (mListener != null) {
                    mListener.onUserReminded(taskId);
                }
            }
        });
    }

    private void onParseError(ParseException e, String taskId) {
        if (mListener != null) {
            mListener.onUserRemindFailed(taskId, e);
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
    public interface HelperInteractionListener {
        /**
         * Handles the successful reminder of a user to finish a task.
         * @param taskId the object id of the task to be finished
         */
        void onUserReminded(@NonNull String taskId);

        /**
         * Handles the failed reminder of a user to finish a task.
         * @param taskId the object id of the task to be finished
         * @param e the {@link ParseException} thrown during the process
         */
        void onUserRemindFailed(@NonNull String taskId, @NonNull ParseException e);
    }
}
