/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the purchase note screen.
 */
public interface AddEditPurchaseNoteViewModel extends ViewModel,
        NoteDialogFragment.DialogInteractionListener {

    @Bindable
    String getNote();

    void setNote(@NonNull String note);

    void onEditNoteMenuClick();

    void onDeleteNoteMenuClick();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

        void showEditNoteDialog(@NonNull String note);

        void showPurchaseScreen();
    }
}
