/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.navdrawer.NavDrawerViewModel;
import ch.giantific.qwittig.presentation.navdrawer.NavDrawerViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the navigation drawer and how to instantiate
 * it.
 */
@Module
public class NavDrawerViewModelModule extends BaseViewModelModule<NavDrawerViewModel.ViewListener> {

    public NavDrawerViewModelModule(@Nullable Bundle savedState,
                                    @NonNull NavDrawerViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    NavDrawerViewModel providesNavDrawerViewModel(@NonNull UserRepository userRepository) {
        return new NavDrawerViewModelImpl(mSavedState, mView, userRepository);
    }

}
