/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.settings.general.SettingsContract;
import ch.giantific.qwittig.presentation.settings.general.SettingsPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the main settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsPresenterModule extends BasePresenterModule {

    public SettingsPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    SettingsContract.Presenter providesSettingsPresenter(@NonNull Navigator navigator,
                                                         @NonNull UserRepository userRepo,
                                                         @NonNull GroupRepository groupRepo) {
        return new SettingsPresenter(savedState, navigator, userRepo, groupRepo);
    }
}
