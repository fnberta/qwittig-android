/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;
import rx.Observable;

/**
 * Defines the actions to take after tasks are updated.
 */
public interface TasksUpdateListener extends BaseWorkerListener {
    /**
     * Sets the {@link Observable} that emits the tasks update stream
     *
     * @param observable the observable emitting the updates
     * @param workerTag  the tag of the worker fragment
     */
    void setTasksUpdateStream(@NonNull Observable<Task> observable, @NonNull String workerTag);
}
