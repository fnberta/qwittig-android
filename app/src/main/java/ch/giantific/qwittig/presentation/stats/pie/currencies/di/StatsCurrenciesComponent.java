/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.currencies.di;

import ch.giantific.qwittig.di.ApplicationComponent;
import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.StatsRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.stats.pie.currencies.StatsCurrenciesFragment;
import dagger.Component;

/**
 * Provides the dependencies for the currencies stats screen.
 */
@PerScreen
@Component(dependencies = {ApplicationComponent.class},
        modules = {StatsCurrenciesViewModelModule.class, RepositoriesModule.class})
public interface StatsCurrenciesComponent {

    void inject(StatsCurrenciesFragment statsCurrenciesFragment);

    StatsRepository getStatsRepo();

    UserRepository getUserRepo();
}
