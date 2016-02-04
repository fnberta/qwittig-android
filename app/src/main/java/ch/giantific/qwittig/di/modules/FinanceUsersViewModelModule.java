/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.viewmodels.FinanceUsersViewModel;
import ch.giantific.qwittig.presentation.viewmodels.FinanceUsersViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class FinanceUsersViewModelModule extends BaseViewModelModule<FinanceUsersViewModel.ViewListener> {

    public FinanceUsersViewModelModule(@Nullable Bundle savedState,
                                       @NonNull FinanceUsersViewModel.ViewListener view) {
        super(savedState, view);
    }

    @PerFragment
    @Provides
    FinanceUsersViewModel providesFinanceUsersViewModel(@NonNull GroupRepository groupRepository,
                                                        @NonNull UserRepository userRepository) {
        return new FinanceUsersViewModelImpl(mSavedState, mView, groupRepository, userRepository);
    }
}
