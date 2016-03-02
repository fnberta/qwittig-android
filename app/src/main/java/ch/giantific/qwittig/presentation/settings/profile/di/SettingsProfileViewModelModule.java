/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.UserRepository;
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
public class SettingsProfileViewModelModule extends BaseViewModelModule<SettingsProfileViewModel.ViewListener> {

    public SettingsProfileViewModelModule(@Nullable Bundle savedState,
                                          @NonNull SettingsProfileViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    SettingsProfileViewModel providesSettingsProfileViewModel(@NonNull UserRepository userRepository) {
        return new SettingsProfileViewModelImpl(mSavedState, mView, userRepository);
    }
}
