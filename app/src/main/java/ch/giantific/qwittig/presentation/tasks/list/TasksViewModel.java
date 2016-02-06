/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.list;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;
import ch.giantific.qwittig.presentation.tasks.addedit.TaskAddActivity;

/**
 * Created by fabio on 09.01.16.
 */
public interface TasksViewModel extends
        OnlineListViewModel<Task>,
        TasksRecyclerAdapter.AdapterInteractionListener,
        TasksUpdateWorkerListener,
        TaskReminderListener {

    int TYPE_ITEM = 0;
    int TYPE_HEADER = 1;

    void onAddTaskFabClick(View view);

    void onDeadlineSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    interface ViewListener extends OnlineListViewModel.ViewListener {
        void loadUpdateTasksWorker();

        void startTaskDetailsActivity(@NonNull Task task);

        /**
         * Starts {@link TaskAddActivity} to let the user add a new {@link Task}.
         */
        void startTaskAddActivity();

        void loadRemindUserWorker(@NonNull String taskId);
    }
}
