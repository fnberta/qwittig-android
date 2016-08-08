/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.di;

import ch.giantific.qwittig.data.repositories.StatsRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.stats.StatsActivity;
import ch.giantific.qwittig.presentation.stats.pie.currencies.StatsCurrenciesFragment;
import ch.giantific.qwittig.presentation.stats.pie.stores.StatsStoresFragment;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingFragment;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the spending stats screen.
 */
@PerActivity
@Subcomponent(modules = {StatsSpendingViewModelModule.class, StatsCurrenciesViewModelModule.class,
        StatsStoresViewModelModule.class})
public interface StatsSubcomponent {

    void inject(StatsActivity statsActivity);

    void inject(StatsSpendingFragment statsSpendingFragment);

    void inject(StatsStoresFragment statsStoresFragment);

    void inject(StatsCurrenciesFragment statsCurrenciesFragment);

    UserRepository getUserRepository();

    StatsRepository getStatsRepository();
}
