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
import ch.giantific.qwittig.presentation.login.LoginFirstGroupViewModel;
import ch.giantific.qwittig.presentation.login.LoginFirstGroupViewModelImpl;
import ch.giantific.qwittig.presentation.login.LoginProfileViewModel;
import ch.giantific.qwittig.presentation.login.LoginProfileViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login accounts screen and how to
 * instantiate it.
 */
@Module
public class LoginFirstGroupViewModelModule extends BaseViewModelModule<LoginFirstGroupViewModel.ViewListener> {

    public LoginFirstGroupViewModelModule(@Nullable Bundle savedState,
                                          @NonNull LoginFirstGroupViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    LoginFirstGroupViewModel providesLoginFirstGroupViewModel(@NonNull UserRepository userRepository) {
        return new LoginFirstGroupViewModelImpl(mSavedState, mView, userRepository);
    }
}
