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
import ch.giantific.qwittig.presentation.login.LoginInvitationViewModel;
import ch.giantific.qwittig.presentation.login.LoginInvitationViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login accounts screen and how to
 * instantiate it.
 */
@Module
public class LoginInvitationViewModelModule extends BaseViewModelModule<LoginInvitationViewModel.ViewListener> {

    private final String mGroupName;
    private final String mInviterNickname;

    public LoginInvitationViewModelModule(@Nullable Bundle savedState,
                                          @NonNull LoginInvitationViewModel.ViewListener view,
                                          @NonNull String groupName,
                                          @NonNull String inviterNickname) {
        super(savedState, view);

        mGroupName = groupName;
        mInviterNickname = inviterNickname;
    }

    @PerScreen
    @Provides
    LoginInvitationViewModel providesLoginInvitationViewModel(@NonNull UserRepository userRepository) {
        return new LoginInvitationViewModelImpl(mSavedState, mView, userRepository, mGroupName, mInviterNickname);
    }
}
