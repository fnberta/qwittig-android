/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import javax.inject.Inject;

import ch.giantific.qwittig.data.repositories.TaskRepository;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Calls cloud functions to remind a user that he/she should finish a task.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class TaskRemindWorker extends BaseWorker<String, TaskRemindWorkerListener> {

    private static final String WORKER_TAG = TaskRemindWorker.class.getCanonicalName();
    private static final String KEY_TASK_ID = "TASK_ID";

    @Inject
    TaskRepository taskRepo;
    private String taskId;

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
            worker = new TaskRemindWorker();
            final Bundle args = new Bundle();
            args.putString(KEY_TASK_ID, taskId);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG + taskId)
                    .commit();
        }

        return worker;
    }

    @Override
    protected void onError() {
        activity.onWorkerError(WORKER_TAG + taskId);
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    protected Observable<String> getObservable(@NonNull Bundle args) {
//        taskId = args.getString(KEY_TASK_ID, "");
//        if (!TextUtils.isEmpty(taskId)) {
//            return taskRepo.pushTaskReminder(taskId).toObservable();
//        }

        // TODO: remind user
        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<String> observable) {
        activity.setTaskReminderStream(observable.toSingle(), taskId, WORKER_TAG + taskId);
    }
}
