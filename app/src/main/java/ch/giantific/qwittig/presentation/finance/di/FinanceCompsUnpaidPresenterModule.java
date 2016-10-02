/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.CompensationRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidContract;
import ch.giantific.qwittig.presentation.finance.unpaid.CompsUnpaidPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the unpaid compensations view model and how to
 * instantiate it.
 */
@Module
public class FinanceCompsUnpaidPresenterModule extends BasePresenterModule {

    public FinanceCompsUnpaidPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    CompsUnpaidContract.Presenter providesFinanceCompsUnpaidPresenter(@NonNull Navigator navigator,
                                                                      @NonNull UserRepository userRepo,
                                                                      @NonNull CompensationRepository compsRepo) {
        return new CompsUnpaidPresenter(savedState, navigator, userRepo, compsRepo);
    }
}
