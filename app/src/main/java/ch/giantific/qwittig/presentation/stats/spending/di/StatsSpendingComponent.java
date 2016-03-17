/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.spending.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.StatsRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.stats.spending.StatsSpendingFragment;
import dagger.Component;

/**
 * Provides the dependencies for the spending stats screen.
 */
@PerScreen
@Component(modules = {StatsSpendingViewModelModule.class, RepositoriesModule.class})
public interface StatsSpendingComponent {

    void inject(StatsSpendingFragment statsSpendingFragment);

    StatsRepository getStatsRepo();

    UserRepository getUserRepo();
}
