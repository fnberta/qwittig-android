/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddEditPurchaseNoteViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddEditPurchaseNoteViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the purchase note screen and how to instantiate it.
 */
@Module
public class AddEditPurchaseNoteViewModelModule extends BaseViewModelModule<AddEditPurchaseNoteViewModel.ViewListener> {

    private final String mNote;

    public AddEditPurchaseNoteViewModelModule(@Nullable Bundle savedState,
                                              @NonNull AddEditPurchaseNoteViewModel.ViewListener view,
                                              @NonNull String note) {
        super(savedState, view);

        mNote = note;
    }

    @PerFragment
    @Provides
    AddEditPurchaseNoteViewModel providesPurchaseNoteViewModel(@NonNull UserRepository userRepository) {
        return new AddEditPurchaseNoteViewModelImpl(mSavedState, mView, userRepository, mNote);
    }
}
