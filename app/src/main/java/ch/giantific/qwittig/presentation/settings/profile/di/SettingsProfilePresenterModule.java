/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileContract;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfilePresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the profile settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsProfilePresenterModule extends BasePresenterModule {

    public SettingsProfilePresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    SettingsProfileContract.Presenter providesSettingsProfilePresenter(@NonNull Navigator navigator,
                                                                       @NonNull UserRepository userRepo,
                                                                       @NonNull GroupRepository groupRepo) {
        return new SettingsProfilePresenter(savedState, navigator, userRepo, groupRepo);
    }
}
