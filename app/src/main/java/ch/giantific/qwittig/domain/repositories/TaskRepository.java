/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;

import java.util.Date;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import rx.Observable;
import rx.Single;

/**
 * Provides the methods to get, update and remove tasks from the local and online data store.
 */
public interface TaskRepository extends Repository {

    /**
     * Saves the task to the local data store.
     *
     * @param task the task to save
     * @param tag  the tag for the local data store
     * @return a {@link Single} representing the save operation
     */
    Single<Task> saveTaskLocalAsync(@NonNull Task task, @NonNull String tag);

    /**
     * Queries the local data store for tasks.
     *
     * @param group    the for which to get the tasks for
     * @param deadline the deadline until which to get queries
     */
    Observable<Task> getTasksLocalAsync(@NonNull Group group, @NonNull Date deadline);

    /**
     * Queries the local data store for a single task.
     *
     * @param taskId the object id of the task to query
     */
    Single<Task> getTaskLocalAsync(@NonNull String taskId);

    /**
     * Fetches the data of a task from the local data store.
     *
     * @param taskId the object id of the task to fetch
     */
    Single<Task> fetchTaskDataLocalAsync(@NonNull String taskId);

    /**
     * Removes a task from the local data store.
     *
     * @param taskId the object id of the task to remove
     * @return whether the removal was successful or not
     */
    boolean removeTaskLocal(@NonNull String taskId);

    /**
     * Updates all tasks in the local data store by deleting all tasks from the local data
     * store, querying and saving new ones.
     *
     * @param currentUser the groups for which to update the tasks
     */
    Observable<Task> updateTasksAsync(@NonNull User currentUser);

    /**
     * Deletes all tasks from the local data store and saves new ones.
     *
     * @param currentUser the groups for which to update the tasks
     * @return whether the update was successful or not
     */
    boolean updateTasks(@NonNull User currentUser);

    /**
     * Updates a task if is already available in the local data store (by simply querying it) or
     * saves it for the first time to the local data store if not.
     *
     * @param taskId the object id of the task to query
     * @param isNew  whether the task is already available in the local data store or not
     * @return whether the update was successful or not
     */
    boolean updateTask(@NonNull String taskId, boolean isNew);

    /**
     * Returns the a {@link Task} object with its data fetched.
     *
     * @param taskId the object id of the task to fetch
     * @return a fetched {@link Task} object
     */
    Task fetchTaskDataLocal(@NonNull String taskId);
}
