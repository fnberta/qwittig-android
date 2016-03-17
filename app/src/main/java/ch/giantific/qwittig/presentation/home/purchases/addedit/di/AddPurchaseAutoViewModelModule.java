/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddEditPurchaseViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddPurchaseViewModelAutoImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the purchase add auto screen and how to instantiate it.
 */
@Module
public class AddPurchaseAutoViewModelModule extends BaseViewModelModule<AddEditPurchaseViewModel.ViewListener> {

    public AddPurchaseAutoViewModelModule(@Nullable Bundle savedState,
                                          @NonNull AddEditPurchaseViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    AddEditPurchaseViewModel providesPurchaseAddAutoViewModel(@NonNull UserRepository userRepository,
                                                              @NonNull PurchaseRepository purchaseRepository) {
        return new AddPurchaseViewModelAutoImpl(mSavedState, mView, userRepository, purchaseRepository);
    }
}
