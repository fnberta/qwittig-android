/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.assignments.addedit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.assignments.addedit.viewmodels.items.AssignmentAddEditIdentityItemViewModel;
import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;

/**
 * Defines an observable view model for the addItemAtPosition or edit assignment screen.
 */
public interface AssignmentAddEditContract {

    interface Presenter extends BasePresenter<ViewListener>,
            DiscardChangesDialogFragment.DialogInteractionListener,
            DatePickerDialog.OnDateSetListener,
            AssignmentAddEditIdentitiesRecyclerAdapter.AdapterInteractionListener {

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
        void onIdentityMove(int fromPosition, int toPosition);

        /**
         * Called when an item has been dismissed by a swipe.
         *
         * @param position the position of the item dismissed
         */
        void onIdentityDismiss(int position);

        void onDeadlineClicked(View view);

        /**
         * Saves the new assignment object if the title is not empty. If it is a one-time
         * assignment, checks if there is exactly one user involved selected.
         */
        void onSaveAssignmentClick(View view);

        void onTimeFrameSelected(@NonNull AdapterView<?> parent, View view, int position, long id);

        void onIdentitiesRowItemClick(@NonNull AssignmentAddEditIdentityItemViewModel itemViewModel);
    }

    interface ViewListener extends BaseView {
        void showDiscardChangesDialog();

        void showDatePickerDialog();

        boolean isIdentitiesEmpty();

        void addIdentity(@NonNull AssignmentAddEditIdentityItemViewModel item);

        void startDragIdentity(@NonNull RecyclerView.ViewHolder viewHolder);

        void swapIdentity(int fromPosition, int toPosition);

        void removeIdentityAtPosition(int position);

        void notifyIdentityChanged(@NonNull AssignmentAddEditIdentityItemViewModel item);
    }

    @IntDef({AssignmentResult.SAVED,
            AssignmentResult.DISCARDED, Activity.RESULT_CANCELED})
    @Retention(RetentionPolicy.SOURCE)
    @interface AssignmentResult {
        int SAVED = 2;
        int DISCARDED = 3;
    }
}
