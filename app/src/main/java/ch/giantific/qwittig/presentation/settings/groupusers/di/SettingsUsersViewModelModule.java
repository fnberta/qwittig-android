/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the manage users settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsUsersViewModelModule extends BaseViewModelModule {

    public SettingsUsersViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    SettingsUsersViewModel providesSettingsAddUsersViewModel(@NonNull Navigator navigator,
                                                             @NonNull RxBus<Object> eventBus,
                                                             @NonNull UserRepository userRepository,
                                                             @NonNull GroupRepository groupRepository) {
        return new SettingsUsersViewModelImpl(savedState, navigator, eventBus, userRepository,
                groupRepository);
    }

}
