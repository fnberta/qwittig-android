/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddEditPurchaseViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.EditPurchaseDraftViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.EditPurchaseDraftViewModelImpl;
import ch.giantific.qwittig.presentation.home.purchases.addedit.EditPurchaseViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the edit purchase screen and how to instantiate it.
 */
@Module
public class EditPurchaseViewModelModule extends BaseViewModelModule<AddEditPurchaseViewModel.ViewListener> {

    private final String mEditPurchaseId;

    public EditPurchaseViewModelModule(@Nullable Bundle savedState,
                                       @NonNull AddEditPurchaseViewModel.ViewListener view,
                                       @NonNull String editPurchaseId) {
        super(savedState, view);

        mEditPurchaseId = editPurchaseId;
    }

    @PerFragment
    @Provides
    AddEditPurchaseViewModel providesPurchaseEditViewModel(@NonNull UserRepository userRepository,
                                                           @NonNull PurchaseRepository purchaseRepository) {
        return new EditPurchaseViewModelImpl(mSavedState, mView, userRepository, purchaseRepository,
                mEditPurchaseId);
    }

    @PerFragment
    @Provides
    EditPurchaseDraftViewModel providesPurchaseEditDraftViewModel(@NonNull UserRepository userRepository,
                                                                  @NonNull PurchaseRepository purchaseRepository) {
        return new EditPurchaseDraftViewModelImpl(mSavedState, mView, userRepository,
                purchaseRepository, mEditPurchaseId);
    }
}
