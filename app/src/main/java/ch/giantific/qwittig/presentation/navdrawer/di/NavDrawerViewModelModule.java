/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
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
public class NavDrawerViewModelModule extends BaseViewModelModule {

    public NavDrawerViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    NavDrawerViewModel providesNavDrawerViewModel(@NonNull Navigator navigator,
                                                  @NonNull RxBus<Object> eventBus,
                                                  @NonNull UserRepository userRepository) {
        return new NavDrawerViewModelImpl(savedState, navigator, eventBus, userRepository);
    }
}
