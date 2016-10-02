/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.login.profile.LoginProfileContract;
import ch.giantific.qwittig.presentation.login.profile.LoginProfilePresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login accounts screen and how to
 * instantiate it.
 */
@Module
public class LoginProfilePresenterModule extends BasePresenterModule {

    public LoginProfilePresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    LoginProfileContract.Presenter providesLoginProfilePresenter(@NonNull Navigator navigator,
                                                                 @NonNull UserRepository userRepo) {
        return new LoginProfilePresenter(savedState, navigator, userRepo);
    }
}
