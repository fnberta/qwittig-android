/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.login.LoginInvitationViewModel;
import ch.giantific.qwittig.presentation.login.LoginInvitationViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login accounts screen and how to
 * instantiate it.
 */
@Module
public class LoginInvitationViewModelModule extends BaseViewModelModule {

    public LoginInvitationViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    LoginInvitationViewModel providesLoginInvitationViewModel(@NonNull RxBus<Object> eventBus,
                                                              @NonNull UserRepository userRepository) {
        return new LoginInvitationViewModelImpl(mSavedState, eventBus, userRepository);
    }
}
