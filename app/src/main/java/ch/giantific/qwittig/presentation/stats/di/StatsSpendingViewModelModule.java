/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingViewModel;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the spending stats screen and how to
 * instantiate it.
 */
@Module
public class StatsSpendingViewModelModule extends BaseViewModelModule {

    public StatsSpendingViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    StatsSpendingViewModel providesStatsSpendingViewModel(@NonNull RxBus<Object> eventBus,
                                                          @NonNull UserRepository userRepository) {
        return new StatsSpendingViewModelImpl(mSavedState, eventBus, userRepository);
    }
}
