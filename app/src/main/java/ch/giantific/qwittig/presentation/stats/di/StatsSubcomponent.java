/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.di;

import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.di.PersistentViewModelsModule;
import ch.giantific.qwittig.presentation.common.di.SimplePresentersModule;
import ch.giantific.qwittig.presentation.stats.StatsActivity;
import ch.giantific.qwittig.presentation.stats.StatsBarFragment;
import ch.giantific.qwittig.presentation.stats.StatsLoader;
import ch.giantific.qwittig.presentation.stats.StatsPieFragment;
import dagger.Subcomponent;

/**
 * Provides the dependencies for the spending stats screen.
 */
@PerActivity
@Subcomponent(modules = {StatsLoaderModule.class, SimplePresentersModule.class,
        PersistentViewModelsModule.class})
public interface StatsSubcomponent {

    void inject(StatsActivity statsActivity);

    void inject(StatsBarFragment statsBarFragment);

    void inject(StatsPieFragment statsPieFragment);

    StatsLoader getStatsLoader();
}
