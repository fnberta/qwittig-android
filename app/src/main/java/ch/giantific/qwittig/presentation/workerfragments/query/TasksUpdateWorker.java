/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import rx.Observable;

/**
 * Performs an online query to the Parse.com database to query tasks.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class TasksUpdateWorker extends BaseQueryWorker<Task, TasksUpdateListener> {

    public static final String WORKER_TAG = "TASK_QUERY_WORKER";
    private static final String LOG_TAG = TasksUpdateWorker.class.getSimpleName();
    @Inject
    TaskRepository mTaskRepo;

    @Override
    protected String getWorkerTag() {
        return WORKER_TAG;
    }

    @Nullable
    @Override
    protected Observable<Task> getObservable(@NonNull Bundle args) {
        if (setCurrentGroups()) {
            return mTaskRepo.updateTasksAsync(mCurrentUserGroups);
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Task> observable, @NonNull String workerTag) {
        mActivity.setTasksUpdateStream(observable, workerTag);
    }
}
