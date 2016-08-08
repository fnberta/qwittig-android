/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.tasks.list.itemmodels.TasksItem;
import ch.giantific.qwittig.presentation.tasks.list.itemmodels.TasksItemModel;

/**
 * Defines an observable view model for the task list screen.
 */
public interface TasksViewModel extends ListViewModel<TasksItemModel, TasksViewModel.ViewListener>,
        TaskRemindWorkerListener {

    void onTaskRowClick(@NonNull TasksItem itemModel);

    void onDoneButtonClick(@NonNull TasksItem itemModel);

    void onRemindButtonClick(@NonNull TasksItem itemModel);

    void onAddTaskFabClick(View view);

    void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
        void loadRemindUserWorker(@NonNull String taskId);
    }
}
