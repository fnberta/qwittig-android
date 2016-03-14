/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.currencies.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.StatsRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.stats.pie.currencies.StatsCurrenciesFragment;
import dagger.Component;

/**
 * Provides the dependencies for the currencies stats screen.
 */
@PerFragment
@Component(modules = {StatsCurrenciesViewModelModule.class, RepositoriesModule.class})
public interface StatsCurrenciesComponent {

    void inject(StatsCurrenciesFragment statsCurrenciesFragment);

    StatsRepository getStatsRepo();

    UserRepository getUserRepo();
}
