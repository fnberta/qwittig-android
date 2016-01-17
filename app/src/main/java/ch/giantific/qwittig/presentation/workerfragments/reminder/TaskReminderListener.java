/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.reminder;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after a user was reminded or after the process failed.
 */
public interface TaskReminderListener extends BaseWorkerListener {

    /**
     * Handles the successful reminder of a user to finish a task.
     *  @param single the object id of the task to be finished
     * @param workerTag the tag of the worker fragment
     */
    void setTaskReminderStream(@NonNull Single<String> single,
                               @NonNull String taskId,
                               @NonNull String workerTag);
}
