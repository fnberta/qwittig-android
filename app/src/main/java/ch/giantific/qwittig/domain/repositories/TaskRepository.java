/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.domain.models.TaskHistoryEvent;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove tasks from the local and online data store.
 */
public interface TaskRepository extends BaseRepository {

    /**
     * Saves the task to the local data store.
     *
     * @param task the task to save
     * @return a {@link Single} emitting the result
     */
    Single<Task> saveTask(@NonNull Task task);

    /**
     * Queries the local data store for tasks.
     *
     * @param identity the for which to get the tasks for
     * @param deadline the deadline until which to get queries
     * @return an {@link Observable} emitting the results
     */
    Observable<Task> getTasks(@NonNull Identity identity, @NonNull Date deadline);

    /**
     * Queries the local data store for a single task.
     *
     * @param taskId the object id of the task to query
     * @return a {@link Single} emitting the result
     */
    Single<Task> getTask(@NonNull String taskId);

    Observable<TaskHistoryEvent> getTaskHistoryEvents(@NonNull Task task);

    Single<TaskHistoryEvent> saveTaskHistoryEvent(@NonNull TaskHistoryEvent taskHistoryEvent);

    /**
     * Fetches the data of a task from the local data store.
     *
     * @param taskId the object id of the task to fetch
     * @return a {@link Single} emitting the result
     */
    Single<Task> fetchTaskData(@NonNull String taskId);

    /**
     * Removes a task from the local data store.
     *
     * @param taskId the object id of the task to remove
     * @return whether the removal was successful or not
     */
    boolean removeTaskLocal(@NonNull String taskId);

    /**
     * Deletes all tasks from the local data store and saves new ones.
     *
     * @param identities the groups for which to update the tasks
     * @return whether the update was successful or not
     */
    boolean updateTasks(@NonNull List<Identity> identities);

    /**
     * Updates a task if is already available in the local data store (by simply querying it) or
     * saves it for the first time to the local data store if not.
     *
     * @param taskId the object id of the task to query
     * @param isNew  whether the task is already available in the local data store or not
     * @return whether the update was successful or not
     */
    boolean updateTask(@NonNull String taskId, boolean isNew);

    boolean setTaskDone(@NonNull String taskId, @NonNull Identity identity);

    /**
     * Sends a push notification to remind a user to finish a task.
     *
     * @param taskId the object id of the task that should be finished
     * @return a {@link Single} emitting the result
     */
    Single<String> pushTaskReminder(@NonNull String taskId);
}
