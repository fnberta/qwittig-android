/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileViewModel;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the profile settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsProfileViewModelModule extends BaseViewModelModule {

    public SettingsProfileViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    SettingsProfileViewModel providesSettingsProfileViewModel(@NonNull Navigator navigator,
                                                              @NonNull RxBus<Object> eventBus,
                                                              @NonNull UserRepository userRepository) {
        return new SettingsProfileViewModelImpl(mSavedState, navigator, eventBus, userRepository);
    }
}