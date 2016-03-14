/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.stores.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.stats.pie.stores.StatsStoresViewModel;
import ch.giantific.qwittig.presentation.stats.pie.stores.StatsStoresViewModelImpl;
import ch.giantific.qwittig.presentation.stats.models.Month;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the stores stats screen and how to
 * instantiate it.
 */
@Module
public class StatsStoresViewModelModule extends BaseViewModelModule<StatsStoresViewModel.ViewListener> {

    private final String mYear;
    private final Month mMonth;

    public StatsStoresViewModelModule(@Nullable Bundle savedState,
                                      @NonNull StatsStoresViewModel.ViewListener view,
                                      @NonNull String year, @NonNull Month month) {
        super(savedState, view);

        mYear = year;
        mMonth = month;
    }

    @PerFragment
    @Provides
    StatsStoresViewModel providesStatsStoresViewModel(@NonNull UserRepository userRepository) {
        return new StatsStoresViewModelImpl(mSavedState, mView, userRepository, mYear, mMonth);
    }
}
