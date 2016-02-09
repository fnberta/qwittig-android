/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.settings.addusers.SettingsAddUsersViewModel;
import ch.giantific.qwittig.presentation.settings.addusers.SettingsAddUsersViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class SettingsAddUsersViewModelModule extends BaseViewModelModule<SettingsAddUsersViewModel.ViewListener> {

    public SettingsAddUsersViewModelModule(@Nullable Bundle savedState,
                                           @NonNull SettingsAddUsersViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    SettingsAddUsersViewModel providesSettingsAddUsersViewModel(@NonNull UserRepository userRepository,
                                                                @NonNull IdentityRepository identityRepository) {
        return new SettingsAddUsersViewModelImpl(mSavedState, mView, identityRepository, userRepository);
    }

}
