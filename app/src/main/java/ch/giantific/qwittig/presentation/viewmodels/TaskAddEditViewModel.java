/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.app.DatePickerDialog;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import java.util.Date;

import ch.giantific.qwittig.domain.models.parse.Task;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.ui.adapters.ItemTouchHelperAdapter;
import ch.giantific.qwittig.presentation.ui.adapters.TaskAddEditUsersRecyclerAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.DiscardChangesDialogFragment;

/**
 * Created by fabio on 09.01.16.
 */
public interface TaskAddEditViewModel extends
        ViewModel<TaskAddEditViewModel.ViewListener>,
        DiscardChangesDialogFragment.DialogInteractionListener,
        DatePickerDialog.OnDateSetListener,
        TaskAddEditUsersRecyclerAdapter.AdapterInteractionListener,
        ItemTouchHelperAdapter {

    @Bindable
    String getTaskTitle();

    void setTaskTitle(@NonNull String taskTitle);

    @Bindable
    Date getTaskDeadline();

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

    User getUserAvailableAtPosition(int position);

    boolean isUserAtPositionInvolved(int position);

    int getItemCount();

    void onDeadlineClicked(View view);

    /**
     * Saves the new {@link Task} object if the title is not empty. If it is a one-time
     * task, checks if there is exactly one user involved selected.
     */
    void onFabSaveTaskClick(View view);

    void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

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
    }}
