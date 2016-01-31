/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.ComparatorParseUserIgnoreCase;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsPaidViewModel;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsPaidViewModelImpl;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.viewmodels.FinanceCompsUnpaidViewModelImpl;
import ch.giantific.qwittig.presentation.viewmodels.FinanceUsersViewModel;
import ch.giantific.qwittig.presentation.viewmodels.FinanceUsersViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class FinanceViewModelModule extends BaseViewModelModule {

    public FinanceViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

//    @PerFragment
//    @Provides
//    ComparatorParseUserIgnoreCase providesComparatorParseUserIgnoreCase() {
//        return new ComparatorParseUserIgnoreCase();
//    }

    @PerFragment
    @Provides
    FinanceUsersViewModel providesFinanceUsersViewModel(@NonNull GroupRepository groupRepository,
                                                        @NonNull UserRepository userRepository) {
        return new FinanceUsersViewModelImpl(mSavedState, groupRepository, userRepository);
    }

    @PerFragment
    @Provides
    FinanceCompsPaidViewModel providesFinanceCompsPaidViewModel(@NonNull GroupRepository groupRepository,
                                                                @NonNull UserRepository userRepository,
                                                                @NonNull CompensationRepository compsRepository) {
        return new FinanceCompsPaidViewModelImpl(mSavedState, groupRepository, userRepository, compsRepository);
    }

    @PerFragment
    @Provides
    FinanceCompsUnpaidViewModel providesFinanceCompsUnpaidViewModel(@NonNull GroupRepository groupRepository,
                                                                    @NonNull UserRepository userRepository,
                                                                    @NonNull CompensationRepository compsRepository) {
        return new FinanceCompsUnpaidViewModelImpl(mSavedState, groupRepository, userRepository, compsRepository);
    }
}
