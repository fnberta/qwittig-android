/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddActivity;
import ch.giantific.qwittig.presentation.tasks.list.items.TasksBaseItem;

/**
 * Defines an observable view model for the task list screen.
 */
public interface TasksViewModel extends OnlineListViewModel<TasksBaseItem>,
        TasksRecyclerAdapter.AdapterInteractionListener,
        TaskRemindWorkerListener {

    void onAddTaskFabClick(View view);

    void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends OnlineListViewModel.ViewListener {
        void startUpdateTasksService();

        void startTaskDetailsScreen(@NonNull Task task);

        /**
         * Starts {@link TaskAddActivity} to let the user add a new {@link Task}.
         */
        void startTaskAddScreen();

        void loadRemindUserWorker(@NonNull String taskId);
    }
}
