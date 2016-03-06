/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.settings.users.SettingsUsersViewModel;
import ch.giantific.qwittig.presentation.settings.users.SettingsUsersViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the manage users settings screen and how to
 * instantiate it.
 */
@Module
public class SettingsUsersViewModelModule extends BaseViewModelModule<SettingsUsersViewModel.ViewListener> {

    public SettingsUsersViewModelModule(@Nullable Bundle savedState,
                                        @NonNull SettingsUsersViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    SettingsUsersViewModel providesSettingsAddUsersViewModel(@NonNull UserRepository userRepository) {
        return new SettingsUsersViewModelImpl(mSavedState, mView, userRepository);
    }

}
