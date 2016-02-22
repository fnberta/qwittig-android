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
import ch.giantific.qwittig.presentation.home.purchases.addedit.PurchaseAddEditViewModelAddAutoImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the purchase add auto screen and how to instantiate it.
 */
@Module
public class PurchaseAddAutoViewModelModule extends BaseViewModelModule<PurchaseAddEditViewModel.ViewListener> {

    public PurchaseAddAutoViewModelModule(@Nullable Bundle savedState,
                                          @NonNull PurchaseAddEditViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    PurchaseAddEditViewModel providesPurchaseAddAutoViewModel(@NonNull IdentityRepository identityRepository,
                                                              @NonNull UserRepository userRepository,
                                                              @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseAddEditViewModelAddAutoImpl(mSavedState, mView, identityRepository,
                userRepository, purchaseRepository);
    }
}
