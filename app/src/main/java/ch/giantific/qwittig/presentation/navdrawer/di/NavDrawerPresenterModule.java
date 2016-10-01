/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.navdrawer.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.navdrawer.NavDrawerContract;
import ch.giantific.qwittig.presentation.navdrawer.NavDrawerPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the navigation drawer and how to instantiate
 * it.
 */
@Module
public class NavDrawerPresenterModule extends BasePresenterModule {

    public NavDrawerPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    NavDrawerContract.Presenter providesNavDrawerPresenter(@NonNull Navigator navigator,
                                                           @NonNull UserRepository userRepo) {
        return new NavDrawerPresenter(savedState, navigator, userRepo);
    }
}
