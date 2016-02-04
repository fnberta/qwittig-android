/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.viewmodels.NavDrawerViewModel;
import ch.giantific.qwittig.presentation.viewmodels.NavDrawerViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class NavDrawerViewModelModule extends BaseViewModelModule<NavDrawerViewModel.ViewListener> {

    public NavDrawerViewModelModule(@Nullable Bundle savedState,
                                    @NonNull NavDrawerViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerActivity
    @Provides
    NavDrawerViewModel providesNavDrawerViewModel(@NonNull GroupRepository groupRepository,
                                                  @NonNull UserRepository userRepository) {
        return new NavDrawerViewModelImpl(mSavedState, mView, groupRepository, userRepository);
    }

}
