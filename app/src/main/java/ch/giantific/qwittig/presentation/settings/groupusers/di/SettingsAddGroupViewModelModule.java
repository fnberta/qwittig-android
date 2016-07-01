/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.RemoteConfigRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the add new group settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsAddGroupViewModelModule extends BaseViewModelModule {

    public SettingsAddGroupViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    SettingsAddGroupViewModel providesSettingsAddGroupViewModel(@NonNull RxBus<Object> eventBus,
                                                                @NonNull UserRepository userRepository,
                                                                @NonNull RemoteConfigRepository configRepository) {
        return new SettingsAddGroupViewModelImpl(mSavedState, eventBus, userRepository, configRepository);
    }
}
