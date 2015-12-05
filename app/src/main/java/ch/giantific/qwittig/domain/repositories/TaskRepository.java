/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.domain.repositories;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Task;

/**
 * Provides the methods to get, update and remove tasks from the local and online data store.
 */
public interface TaskRepository {

    /**
     * Queries the local data store for tasks.
     *
     * @param group    the for which to get the tasks for
     * @param deadline the deadline until which to get queries
     * @param listener the callback called when the query finishes
     */
    void getTasksLocalAsync(@NonNull Group group, @NonNull Date deadline,
                            @NonNull GetTasksLocalListener listener);

    /**
     * Queries the local data store for a single task.
     *
     * @param taskId   the object id of the task to query
     * @param listener the callback called when the query finishes
     */
    void getTaskLocalAsync(@NonNull String taskId,
                           @NonNull GetTaskLocalListener listener);

    /**
     * Fetches the data of a task from the local data store.
     *
     * @param taskId   the object id of the task to fetch
     * @param listener the callback when the fetch finishes
     */
    void fetchTaskDataLocalAsync(@NonNull String taskId,
                                 @NonNull GetTaskLocalListener listener);

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
     * @param groups   the groups for which to update the tasks
     * @param listener the callback when a query finishes, fails or all queries are finished
     */
    void updateTasksAsync(@NonNull List<ParseObject> groups,
                          @NonNull UpdateTasksListener listener);

    /**
     * Deletes all tasks from the local data store and saves new ones.
     *
     * @param groups the groups for which to update the tasks
     * @return whether the update was successful or not
     */
    boolean updateTasks(@NonNull List<ParseObject> groups);

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

    /**
     * Defines the callback when tasks are loaded from the local data store.
     */
    interface GetTasksLocalListener {
        /**
         * Called when local tasks were successfully loaded.
         *
         * @param tasks the loaded tasks
         */
        void onTasksLocalLoaded(@NonNull List<ParseObject> tasks);
    }

    /**
     * Defines the callback when a task is loaded from the local data store.
     */
    interface GetTaskLocalListener {
        /**
         * Called when a local task was successfully loaded.
         *
         * @param task the loaded task
         */
        void onTaskLocalLoaded(@NonNull Task task);
    }

    /**
     * Defines the callback when tasks in the local data store are updated from the online data
     * store.
     */
    interface UpdateTasksListener {
        /**
         * Called when local tasks were successfully updated.
         */
        void onTasksUpdated();

        /**
         * Called when tasks update failed.
         *
         * @param errorMessage the error message from the exception thrown in the process
         */
        void onTaskUpdateFailed(@StringRes int errorMessage);
    }
}
