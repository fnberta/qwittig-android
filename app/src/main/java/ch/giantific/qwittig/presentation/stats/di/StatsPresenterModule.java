/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.stats.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.stats.StatsContract;
import ch.giantific.qwittig.presentation.stats.StatsPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the stores stats screen and how to
 * instantiate it.
 */
@Module
public class StatsPresenterModule extends BasePresenterModule {

    public StatsPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    StatsContract.Presenter providesStatsPresenter(@NonNull Navigator navigator,
                                                   @NonNull UserRepository userRepo) {
        return new StatsPresenter(savedState, navigator, userRepo);
    }
}
