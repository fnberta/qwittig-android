/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import javax.inject.Inject;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.repositories.TaskRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseQueryWorker;
import rx.Observable;

/**
 * Performs an online query to the Parse.com database to query tasks.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class TasksUpdateWorker extends BaseQueryWorker<Task, TasksUpdateWorkerListener> {

    private static final String WORKER_TAG = TasksUpdateWorker.class.getCanonicalName();
    @Inject
    TaskRepository mTaskRepo;

    public TasksUpdateWorker() {
        // required empty constructor
    }

    /**
     * Attaches a new instance of a {@link TasksUpdateWorker}.
     *
     * @param fm the fragment manager to use for the transaction
     * @return a new instance of a {@link TasksUpdateWorker}
     */
    public static TasksUpdateWorker attach(@NonNull FragmentManager fm) {
        TasksUpdateWorker worker = (TasksUpdateWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new TasksUpdateWorker();
            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Nullable
    @Override
    protected Observable<Task> getObservable(@NonNull Bundle args) {
        if (checkIdentities()) {
            return mTaskRepo.updateTasksAsync(mIdentities);
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Task> observable) {
        mActivity.setTasksUpdateStream(observable, WORKER_TAG);
    }
}
