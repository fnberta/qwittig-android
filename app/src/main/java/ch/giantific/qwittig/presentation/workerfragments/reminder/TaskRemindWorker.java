/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.reminder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ch.giantific.qwittig.data.repositories.ParseApiRepository;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorker;
import rx.Observable;

/**
 * Calls Parse.com cloud functions to remind a user that he/she should finish a task.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class TaskRemindWorker extends BaseWorker<String, TaskReminderListener> {

    public static final String WORKER_TAG = "TASK_REMIND_WORKER_";
    private static final String BUNDLE_TASK_ID = "BUNDLE_TASK_ID";
    private static final String LOG_TAG = TaskRemindWorker.class.getSimpleName();
    private String mTaskId;
    private ApiRepository mApiRepo;

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
    protected String getWorkerTag() {
        return WORKER_TAG + mTaskId;
    }

    @Nullable
    @Override
    protected Observable<String> getObservable(@NonNull Bundle args) {
        mTaskId = args.getString(BUNDLE_TASK_ID, "");
        if (!TextUtils.isEmpty(mTaskId)) {
            mApiRepo = new ParseApiRepository(); // TODO: inject
            return mApiRepo.pushTaskReminder(mTaskId).toObservable();
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<String> observable, @NonNull String workerTag) {
        mActivity.setTaskReminderStream(observable.toSingle(), mTaskId, workerTag);
    }
}
