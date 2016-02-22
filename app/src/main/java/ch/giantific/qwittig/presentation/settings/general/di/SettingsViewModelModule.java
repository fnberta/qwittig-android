/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.settings.general.SettingsViewModel;
import ch.giantific.qwittig.presentation.settings.general.SettingsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the main settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsViewModelModule extends BaseViewModelModule<SettingsViewModel.ViewListener> {

    public SettingsViewModelModule(@Nullable Bundle savedState,
                                   @NonNull SettingsViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    SettingsViewModel providesSettingsViewModel(@NonNull UserRepository userRepository,
                                                @NonNull GroupRepository groupRepository,
                                                @NonNull IdentityRepository identityRepository) {
        return new SettingsViewModelImpl(mSavedState, mView, userRepository, groupRepository, identityRepository);
    }
}
