/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.pie.stores.di;

import ch.giantific.qwittig.di.RepositoriesModule;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.StatsRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.stats.pie.stores.StatsStoresFragment;
import dagger.Component;

/**
 * Provides the dependencies for the stores stats screen.
 */
@PerScreen
@Component(modules = {StatsStoresViewModelModule.class, RepositoriesModule.class})
public interface StatsStoresComponent {

    void inject(StatsStoresFragment statsStoresFragment);

    StatsRepository getStatsRepo();

    UserRepository getUserRepo();
}
