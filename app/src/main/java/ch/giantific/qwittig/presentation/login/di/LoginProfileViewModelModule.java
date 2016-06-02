/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
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
public class LoginProfileViewModelModule extends BaseViewModelModule<LoginProfileViewModel.ViewListener> {

    public LoginProfileViewModelModule(@Nullable Bundle savedState,
                                       @NonNull LoginProfileViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    LoginProfileViewModel providesLoginProfileViewModel(@NonNull UserRepository userRepository) {
        return new LoginProfileViewModelImpl(mSavedState, mView, userRepository);
    }
}
