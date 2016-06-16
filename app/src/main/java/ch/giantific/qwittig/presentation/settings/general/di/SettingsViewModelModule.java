/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
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

    @PerScreen
    @Provides
    SettingsViewModel providesSettingsViewModel(@NonNull RxBus<Object> eventBus,
                                                @NonNull UserRepository userRepository) {
        return new SettingsViewModelImpl(mSavedState, mView, eventBus, userRepository);
    }
}
