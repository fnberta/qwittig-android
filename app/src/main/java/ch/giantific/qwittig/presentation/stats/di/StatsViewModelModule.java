/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.stats.StatsViewModel;
import ch.giantific.qwittig.presentation.stats.StatsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the stores stats screen and how to
 * instantiate it.
 */
@Module
public class StatsViewModelModule extends BaseViewModelModule {

    public StatsViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    StatsViewModel providesStatsViewModelImpl(@NonNull Navigator navigator,
                                              @NonNull RxBus<Object> eventBus,
                                              @NonNull UserRepository userRepository) {
        return new StatsViewModelImpl(savedState, navigator, eventBus, userRepository);
    }
}
