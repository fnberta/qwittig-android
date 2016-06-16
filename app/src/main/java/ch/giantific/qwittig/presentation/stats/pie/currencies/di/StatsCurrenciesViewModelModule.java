/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.currencies.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.stats.pie.currencies.StatsCurrenciesViewModelImpl;
import ch.giantific.qwittig.presentation.stats.pie.StatsPieViewModel;
import ch.giantific.qwittig.presentation.stats.models.Month;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the currencies stats screen and how to
 * instantiate it.
 */
@Module
public class StatsCurrenciesViewModelModule extends BaseViewModelModule<StatsPieViewModel.ViewListener> {

    private final String mYear;
    private final Month mMonth;

    public StatsCurrenciesViewModelModule(@Nullable Bundle savedState,
                                          @NonNull StatsPieViewModel.ViewListener view,
                                          @NonNull String year, @NonNull Month month) {
        super(savedState, view);

        mYear = year;
        mMonth = month;
    }

    @PerScreen
    @Provides
    StatsPieViewModel providesStatsPieViewModel(@NonNull RxBus<Object> eventBus,
                                                @NonNull UserRepository userRepository) {
        return new StatsCurrenciesViewModelImpl(mSavedState, mView, eventBus, userRepository, mYear, mMonth);
    }
}
