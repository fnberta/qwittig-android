/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.HomeViewModel;
import ch.giantific.qwittig.presentation.home.HomeViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the home view model and how to instantiate it.
 */
@Module
public class HomeViewModelModule extends BaseViewModelModule<HomeViewModel.ViewListener> {

    public HomeViewModelModule(@Nullable Bundle savedState,
                               @NonNull HomeViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    HomeViewModel providesHomeViewModel(@NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepository) {
        return new HomeViewModelImpl(mSavedState, mView, userRepository, purchaseRepository);
    }
}
