/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.reminder;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorker;
import rx.Observable;

/**
 * Calls Parse.com cloud functions to remind a user that he/she should finish a task.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class TaskRemindWorker extends BaseWorker<String, TaskReminderListener> {

    private static final String WORKER_TAG = TaskRemindWorker.class.getCanonicalName();
    private static final String KEY_TASK_ID = "TASK_ID";
    @Inject
    ApiRepository mApiRepo;
    private String mTaskId;

    /**
     * Attaches a new instance of {@link TaskRemindWorker} with an argument.
     *
     * @param fm     the fragment manager to use for the transaction
     * @param taskId the object id of the task that should be finished
     * @return a new instance of {@link TaskRemindWorker}
     */
    public static TaskRemindWorker attach(@NonNull FragmentManager fm, @NonNull String taskId) {
        TaskRemindWorker worker = (TaskRemindWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = TaskRemindWorker.newInstance(taskId);
            fm.beginTransaction()
                    .add(worker, WORKER_TAG + taskId)
                    .commit();
        }

        return worker;
    }

    /**
     * Returns a new instance of {@link TaskRemindWorker} with an argument.
     *
     * @param taskId the object id of the task that should be finished
     * @return a new instance of {@link TaskRemindWorker}
     */
    @NonNull
    private static TaskRemindWorker newInstance(@NonNull String taskId) {
        TaskRemindWorker fragment = new TaskRemindWorker();
        Bundle args = new Bundle();
        args.putString(KEY_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onError() {
        // TODO: check tag
        mActivity.onWorkerError(WORKER_TAG + mTaskId);
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    protected Observable<String> getObservable(@NonNull Bundle args) {
        mTaskId = args.getString(KEY_TASK_ID, "");
        if (!TextUtils.isEmpty(mTaskId)) {
            return mApiRepo.pushTaskReminder(mTaskId).toObservable();
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<String> observable) {
        mActivity.setTaskReminderStream(observable.toSingle(), mTaskId, WORKER_TAG + mTaskId);
    }
}
