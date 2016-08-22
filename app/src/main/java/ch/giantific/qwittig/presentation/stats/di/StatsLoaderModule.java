/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.di;

import android.content.Context;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.data.repositories.StatsRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.presentation.stats.StatsLoader;
import dagger.Module;
import dagger.Provides;

/**
 * Defines the loader to use to provide the stats data.
 */
@Module
public class StatsLoaderModule {

    private final Context context;

    public StatsLoaderModule(@NonNull Context context) {
        this.context = context;
    }

    @Provides
    StatsLoader providesStatsLoader(@NonNull UserRepository userRepository,
                                    @NonNull StatsRepository statsRepository) {
        return new StatsLoader(context, userRepository, statsRepository);
    }
}
