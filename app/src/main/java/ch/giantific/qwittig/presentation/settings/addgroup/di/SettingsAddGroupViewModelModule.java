/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.settings.addgroup.SettingsAddGroupViewModel;
import ch.giantific.qwittig.presentation.settings.addgroup.SettingsAddGroupViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the add new group settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsAddGroupViewModelModule extends BaseViewModelModule<SettingsAddGroupViewModel.ViewListener> {

    public SettingsAddGroupViewModelModule(@Nullable Bundle savedState,
                                           @NonNull SettingsAddGroupViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerScreen
    @Provides
    SettingsAddGroupViewModel providesSettingsAddGroupViewModel(@NonNull UserRepository userRepository) {
        return new SettingsAddGroupViewModelImpl(mSavedState, mView, userRepository);
    }
}
