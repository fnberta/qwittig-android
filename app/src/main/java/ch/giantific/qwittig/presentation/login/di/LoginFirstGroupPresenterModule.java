/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.login.firstgroup.LoginFirstGroupContract;
import ch.giantific.qwittig.presentation.login.firstgroup.LoginFirstGroupPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the login accounts screen and how to
 * instantiate it.
 */
@Module
public class LoginFirstGroupPresenterModule extends BasePresenterModule {

    public LoginFirstGroupPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    LoginFirstGroupContract.Presenter providesLoginFirstGroupPresenter(@NonNull Navigator navigator,
                                                                       @NonNull RemoteConfigHelper configHelper,
                                                                       @NonNull UserRepository userRepos,
                                                                       @NonNull GroupRepository groupRepo) {
        return new LoginFirstGroupPresenter(savedState, navigator, configHelper, userRepos, groupRepo);
    }
}
