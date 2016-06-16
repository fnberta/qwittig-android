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

    private boolean mWithInvitation;

    public LoginProfileViewModelModule(@Nullable Bundle savedState,
                                       @NonNull LoginProfileViewModel.ViewListener view,
                                       boolean withInvitation) {
        super(savedState, view);

        mWithInvitation = withInvitation;
    }

    @PerScreen
    @Provides
    LoginProfileViewModel providesLoginProfileViewModel(@NonNull RxBus<Object> eventBus,
                                                        @NonNull UserRepository userRepository) {
        return new LoginProfileViewModelImpl(mSavedState, mView, eventBus, userRepository, mWithInvitation);
    }
}
