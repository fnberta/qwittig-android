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
import ch.giantific.qwittig.presentation.login.invitation.LoginInvitationContract;
import ch.giantific.qwittig.presentation.login.invitation.LoginInvitationPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login accounts screen and how to
 * instantiate it.
 */
@Module
public class LoginInvitationPresenterModule extends BasePresenterModule {

    public LoginInvitationPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    LoginInvitationContract.Presenter providesLoginInvitationPresenter(@NonNull Navigator navigator,
                                                                       @NonNull UserRepository userRepo) {
        return new LoginInvitationPresenter(savedState, navigator, userRepo);
    }
}
