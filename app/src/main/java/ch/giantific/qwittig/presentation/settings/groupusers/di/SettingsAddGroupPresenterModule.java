/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupContract;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the add new group settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsAddGroupPresenterModule extends BasePresenterModule {

    public SettingsAddGroupPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    SettingsAddGroupContract.Presenter providesSettingsAddGroupPresenter(@NonNull Navigator navigator,
                                                                         @NonNull RemoteConfigHelper configHelper,
                                                                         @NonNull UserRepository userRepo,
                                                                         @NonNull GroupRepository groupRepo) {
        return new SettingsAddGroupPresenter(savedState, navigator, userRepo, groupRepo, configHelper);
    }
}
