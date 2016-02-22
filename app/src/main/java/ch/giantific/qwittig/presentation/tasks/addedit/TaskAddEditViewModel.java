/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.tasks.addedit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Task;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Created by fabio on 09.01.16.
 */
public interface TaskAddEditViewModel extends ViewModel,
        DiscardChangesDialogFragment.DialogInteractionListener,
        DatePickerDialog.OnDateSetListener,
        TaskAddEditUsersRecyclerAdapter.AdapterInteractionListener {

    @Bindable
    String getTaskTitle();

    void setTaskTitle(@NonNull String taskTitle);

    @Bindable
    String getTaskDeadline();

    void setTaskDeadline(@NonNull Date deadline);

    @Bindable
    int getTaskDeadlineVisibility();

    @Bindable
    int getTaskTimeFrame();

    void setTaskTimeFrame(int taskTimeFrame);

    /**
     * Checks whether the user has made an changes to the data on the screen. If yes shows a
     * dialog that asks if the changes should be discarded. If no, finishes.
     */
    void checkForChangesAndExit();

    Identity getIdentityAtPosition(int position);

    boolean isUserAtPositionInvolved(int position);

    int getItemCount();

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
    interface ViewListener extends ViewModel.ViewListener {
        void showDiscardChangesDialog();

        void showDatePickerDialog();

        String getTaskTitle();

        /**
         * Sets the result depending on the action taken and finishes the screen
         *
         * @param taskResult the task according to which to set the result
         */
        void finishScreen(int taskResult);

        void setUserListMinimumHeight(int numberOfUsers);

        void onStartUserDrag(@NonNull RecyclerView.ViewHolder viewHolder);

        void notifyDataSetChanged();

        void notifyItemChanged(int position);

        void notifyItemMoved(int fromPosition, int toPosition);

        void notifyItemRemoved(int position);

        void setTimeFramePosition(int timeFrame);
    }
}
