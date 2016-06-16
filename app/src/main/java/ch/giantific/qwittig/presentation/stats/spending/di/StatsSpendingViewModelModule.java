/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.spending.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingViewModel;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingViewModelImpl;
import ch.giantific.qwittig.presentation.stats.models.Month;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the spending stats screen and how to
 * instantiate it.
 */
@Module
public class StatsSpendingViewModelModule extends BaseViewModelModule<StatsSpendingViewModel.ViewListener> {

    private final String mYear;
    private final Month mMonth;

    public StatsSpendingViewModelModule(@Nullable Bundle savedState,
                                        @NonNull StatsSpendingViewModel.ViewListener view,
                                        @NonNull String year, @NonNull Month month) {
        super(savedState, view);

        mYear = year;
        mMonth = month;
    }

    @PerScreen
    @Provides
    StatsSpendingViewModel providesStatsSpendingViewModel(@NonNull RxBus<Object> eventBus,
                                                          @NonNull UserRepository userRepository) {
        return new StatsSpendingViewModelImpl(mSavedState, mView, eventBus, userRepository, mYear, mMonth);
    }
}
