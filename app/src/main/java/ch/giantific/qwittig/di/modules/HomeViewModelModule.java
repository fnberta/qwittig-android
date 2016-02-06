/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.HomeViewModel;
import ch.giantific.qwittig.presentation.home.HomeViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
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
