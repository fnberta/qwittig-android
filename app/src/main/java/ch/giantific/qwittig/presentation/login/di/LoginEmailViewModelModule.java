/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.login.LoginEmailViewModel;
import ch.giantific.qwittig.presentation.login.LoginEmailViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login/sign-up with email screen and how to
 * instantiate it.
 */
@Module
public class LoginEmailViewModelModule extends BaseViewModelModule<LoginEmailViewModel.ViewListener> {

    public LoginEmailViewModelModule(@Nullable Bundle savedState,
                                     @NonNull LoginEmailViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    LoginEmailViewModel providesLoginEmailViewModel(@NonNull RxBus<Object> eventBus,
                                                    @NonNull UserRepository userRepository) {
        return new LoginEmailViewModelImpl(mSavedState, mView, eventBus, userRepository);
    }
}
