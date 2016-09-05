/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit;

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

import ch.giantific.qwittig.presentation.assignments.addedit.itemmodels.AssignmentAddEditIdentityItemModel;
import ch.giantific.qwittig.presentation.common.ListDragInteraction;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;

/**
 * Defines an observable view model for the add or edit assignment screen.
 */
public interface AssignmentAddEditViewModel extends ListViewModel<AssignmentAddEditIdentityItemModel, AssignmentAddEditViewModel.ViewListener>,
        DiscardChangesDialogFragment.DialogInteractionListener,
        DatePickerDialog.OnDateSetListener,
        AssignmentAddEditIdentitiesRecyclerAdapter.AdapterInteractionListener {

    void setListDragInteraction(@NonNull ListDragInteraction listDragInteraction);

    @Bindable
    String getTitle();

    void setTitle(@NonNull String title);

    @Bindable
    String getDeadline();

    void setDeadline(@NonNull Date deadline);

    @Bindable
    boolean isAsNeeded();

    @Bindable
    int getSelectedTimeFrame();

    void setTimeFrame(int timeFrame);

    int[] getTimeFrames();

    /**
     * Checks whether the user has made an changes to the data on the screen. If yes shows a
     * dialog that asks if the changes should be discarded. If no, finishes.
     */
    void onUpOrBackClick();

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
     * Saves the new assignment object if the title is not empty. If it is a one-time
     * assignment, checks if there is exactly one user involved selected.
     */
    void onFabSaveAssignmentClick(View view);

    void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    void onIdentitiesRowItemClick(@NonNull AssignmentAddEditIdentityItemModel itemModel);

    @IntDef({AssignmentResult.SAVED,
            AssignmentResult.DISCARDED, Activity.RESULT_CANCELED})
    @Retention(RetentionPolicy.SOURCE)
    @interface AssignmentResult {
        int SAVED = 2;
        int DISCARDED = 3;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
        void showDiscardChangesDialog();

        void showDatePickerDialog();
    }
}
