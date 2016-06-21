/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.HomeViewModel;
import ch.giantific.qwittig.presentation.purchases.list.HomeViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the home view model and how to instantiate it.
 */
@Module
public class HomeViewModelModule extends BaseViewModelModule {

    public HomeViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    HomeViewModel providesHomeViewModel(@NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepository) {
        return new HomeViewModelImpl(mSavedState, navigator, eventBus, userRepository, purchaseRepository);
    }
}
