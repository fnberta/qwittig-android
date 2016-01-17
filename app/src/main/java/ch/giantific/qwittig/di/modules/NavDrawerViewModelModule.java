/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.viewmodels.NavDrawerViewModel;
import ch.giantific.qwittig.presentation.viewmodels.NavDrawerViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class NavDrawerViewModelModule {

    public NavDrawerViewModelModule() {
    }

    @PerActivity
    @Provides
    NavDrawerViewModel providesNavDrawerViewModel(@NonNull GroupRepository groupRepository,
                                                  @NonNull UserRepository userRepository) {
        return new NavDrawerViewModelImpl(groupRepository, userRepository);
    }

}
