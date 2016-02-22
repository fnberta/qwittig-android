/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditViewModelEditImpl;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseEditDraftViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseEditDraftViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the edit purchase screen and how to instantiate it.
 */
@Module
public class PurchaseEditViewModelModule extends BaseViewModelModule<PurchaseAddEditViewModel.ViewListener> {

    private final String mEditPurchaseId;

    public PurchaseEditViewModelModule(@Nullable Bundle savedState,
                                       @NonNull PurchaseAddEditViewModel.ViewListener view,
                                       @NonNull String editPurchaseId) {
        super(savedState, view);

        mEditPurchaseId = editPurchaseId;
    }

    @PerFragment
    @Provides
    PurchaseAddEditViewModel providesPurchaseEditViewModel(@NonNull IdentityRepository identityRepository,
                                                           @NonNull UserRepository userRepository,
                                                           @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseAddEditViewModelEditImpl(mSavedState, mView, identityRepository,
                userRepository, purchaseRepository, mEditPurchaseId);
    }

    @PerFragment
    @Provides
    PurchaseEditDraftViewModel providesPurchaseEditDraftViewModel(@NonNull IdentityRepository identityRepository,
                                                                  @NonNull UserRepository userRepository,
                                                                  @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseEditDraftViewModelImpl(mSavedState, mView, identityRepository,
                userRepository, purchaseRepository, mEditPurchaseId);
    }
}
