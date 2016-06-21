/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;
import ch.giantific.qwittig.presentation.tasks.list.items.TasksBaseItem;

/**
 * Defines an observable view model for the task list screen.
 */
public interface TasksViewModel extends OnlineListViewModel<TasksBaseItem, TasksViewModel.ViewListener>,
        TasksRecyclerAdapter.AdapterInteractionListener,
        TaskRemindWorkerListener {

    void onAddTaskFabClick(View view);

    void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends OnlineListViewModel.ViewListener {
        void startUpdateTasksService();

        void loadRemindUserWorker(@NonNull String taskId);
    }
}
