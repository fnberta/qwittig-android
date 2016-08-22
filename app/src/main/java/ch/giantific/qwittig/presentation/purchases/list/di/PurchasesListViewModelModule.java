/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the list of purchases screen and how to
 * instantiate it.
 */
@Module
public class PurchasesListViewModelModule extends BaseViewModelModule {

    public PurchasesListViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    PurchasesViewModel providesHomePurchasesViewModel(@NonNull Navigator navigator,
                                                      @NonNull RxBus<Object> eventBus,
                                                      @NonNull UserRepository userRepository,
                                                      @NonNull PurchaseRepository purchaseRepository) {
        return new PurchasesViewModelImpl(savedState, navigator, eventBus, userRepository,
                purchaseRepository);
    }
}
