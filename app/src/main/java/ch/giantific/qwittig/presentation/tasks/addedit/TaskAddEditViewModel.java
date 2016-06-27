/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.presentation.common.ListDragInteraction;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;

/**
 * Defines an observable view model for the add or edit task screen.
 */
public interface TaskAddEditViewModel extends ListViewModel<Identity, TaskAddEditViewModel.ViewListener>,
        DiscardChangesDialogFragment.DialogInteractionListener,
        DatePickerDialog.OnDateSetListener,
        TaskAddEditUsersRecyclerAdapter.AdapterInteractionListener {

    void setListDragInteraction(@NonNull ListDragInteraction listDragInteraction);

    @Bindable
    String getTaskTitle();

    void setTaskTitle(@NonNull String taskTitle);

    @Bindable
    String getTaskDeadline();

    void setTaskDeadline(@NonNull Date deadline);

    @Bindable
    boolean isAsNeededTask();

    @Bindable
    int getSelectedTimeFrame();

    void setTaskTimeFrame(int taskTimeFrame);

    int[] getTimeFrames();

    /**
     * Checks whether the user has made an changes to the data on the screen. If yes shows a
     * dialog that asks if the changes should be discarded. If no, finishes.
     */
    void onUpOrBackClick();

    float getIdentityAlpha(int position);

    /**
     * Called when an item has been dragged far enough to trigger a move. This is called every time
     * an item is shifted, and not at the end of a "drop" event.
     *
     * @param fromPosition the start position of the moved item
     * @param toPosition   the end position of the moved item
     */
    void onItemMove(int fromPosition, int toPosition);


    /**
     * Called when an item has been dismissed by a swipe.
     *
     * @param position the position of the item dismissed
     */
    void onItemDismiss(int position);

    void onDeadlineClicked(View view);

    void onTitleChanged(CharSequence s, int start, int before, int count);

    /**
     * Saves the new {@link Task} object if the title is not empty. If it is a one-time
     * task, checks if there is exactly one user involved selected.
     */
    void onFabSaveTaskClick(View view);

    void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    @IntDef({TaskAddEditViewModel.TaskResult.TASK_SAVED,
            TaskAddEditViewModel.TaskResult.TASK_DISCARDED, Activity.RESULT_CANCELED})
    @Retention(RetentionPolicy.SOURCE)
    @interface TaskResult {
        int TASK_SAVED = 2;
        int TASK_DISCARDED = 3;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
        void showDiscardChangesDialog();

        void showDatePickerDialog();
    }
}