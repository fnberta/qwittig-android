/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.stats.models.Month;
import ch.giantific.qwittig.presentation.stats.pie.stores.StatsStoresViewModel;
import ch.giantific.qwittig.presentation.stats.pie.stores.StatsStoresViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the stores stats screen and how to
 * instantiate it.
 */
@Module
public class StatsStoresViewModelModule extends BaseViewModelModule {

    public StatsStoresViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    StatsStoresViewModel providesStatsStoresViewModel(@NonNull RxBus<Object> eventBus,
                                                      @NonNull UserRepository userRepository) {
        return new StatsStoresViewModelImpl(mSavedState, eventBus, userRepository);
    }
}
