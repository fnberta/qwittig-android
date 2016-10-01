/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersContract;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the manage users settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsUsersPresenterModule extends BasePresenterModule {

    public SettingsUsersPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    SettingsUsersContract.Presenter providesSettingsUsersPresenter(@NonNull Navigator navigator,
                                                                   @NonNull UserRepository userRepo,
                                                                   @NonNull GroupRepository groupRepo) {
        return new SettingsUsersPresenter(savedState, navigator, userRepo, groupRepo);
    }

}
