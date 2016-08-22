/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.login.LoginProfileViewModel;
import ch.giantific.qwittig.presentation.login.LoginProfileViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login accounts screen and how to
 * instantiate it.
 */
@Module
public class LoginProfileViewModelModule extends BaseViewModelModule {

    public LoginProfileViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    LoginProfileViewModel providesLoginProfileViewModel(@NonNull Navigator navigator,
                                                        @NonNull RxBus<Object> eventBus,
                                                        @NonNull UserRepository userRepository) {
        return new LoginProfileViewModelImpl(savedState, navigator, eventBus, userRepository);
    }
}
