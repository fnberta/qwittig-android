/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.spending.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.StatsRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.stats.pie.currencies.di.StatsCurrenciesViewModelModule;
import ch.giantific.qwittig.presentation.stats.pie.stores.di.StatsStoresViewModelModule;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingFragment;
import ch.giantific.qwittig.presentation.stats.spending.di.StatsSpendingViewModelModule;
import dagger.Component;

/**
 * Provides the dependencies for the spending stats screen.
 */
@PerFragment
@Component(modules = {StatsSpendingViewModelModule.class, RepositoriesModule.class})
public interface StatsSpendingComponent {

    void inject(StatsSpendingFragment statsSpendingFragment);

    StatsRepository getStatsRepo();

    UserRepository getUserRepo();
}
