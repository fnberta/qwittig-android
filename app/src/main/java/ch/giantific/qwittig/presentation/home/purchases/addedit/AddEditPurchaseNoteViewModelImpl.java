/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;

/**
 * Created by fabio on 23.02.16.
 */
public class AddEditPurchaseNoteViewModelImpl extends ViewModelBaseImpl<AddEditPurchaseNoteViewModel.ViewListener>
        implements AddEditPurchaseNoteViewModel {

    private String mNote;

    public AddEditPurchaseNoteViewModelImpl(@Nullable Bundle savedState,
                                            @NonNull AddEditPurchaseNoteViewModel.ViewListener view,
                                            @NonNull UserRepository userRepository,
                                            @NonNull String note) {
        super(savedState, view, userRepository);

        mNote = note;
    }

    @Override
    @Bindable
    public String getNote() {
        return mNote;
    }

    @Override
    public void setNote(@NonNull String note) {
        mNote = note;
        notifyPropertyChanged(BR.note);
    }

    @Override
    public void onEditNoteMenuClick() {
        mView.showEditNoteDialog(mNote);
    }

    @Override
    public void onNoteSet(@NonNull String note) {
        setNote(note);
        mView.showMessage(R.string.toast_note_edited);
    }
}
