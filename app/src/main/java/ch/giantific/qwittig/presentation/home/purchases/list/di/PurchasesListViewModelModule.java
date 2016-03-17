/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesViewModel;
import ch.giantific.qwittig.presentation.home.purchases.list.PurchasesViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the list of purchases screen and how to
 * instantiate it.
 */
@Module
public class PurchasesListViewModelModule extends BaseViewModelModule<PurchasesViewModel.ViewListener> {

    public PurchasesListViewModelModule(@Nullable Bundle savedState,
                                        @NonNull PurchasesViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    PurchasesViewModel providesHomePurchasesViewModel(@NonNull UserRepository userRepository,
                                                      @NonNull PurchaseRepository purchaseRepository) {
        return new PurchasesViewModelImpl(mSavedState, mView, userRepository, purchaseRepository);
    }
}
