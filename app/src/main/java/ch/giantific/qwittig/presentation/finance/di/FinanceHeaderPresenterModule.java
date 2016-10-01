/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.finance.FinanceHeaderContract;
import ch.giantific.qwittig.presentation.finance.FinanceHeaderPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the balance header view model and how to instantiate it.
 */
@Module
public class FinanceHeaderPresenterModule extends BasePresenterModule {

    public FinanceHeaderPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    FinanceHeaderContract.Presenter providesFinanceHeaderPresenter(@NonNull Navigator navigator,
                                                                   @NonNull UserRepository userRepo) {
        return new FinanceHeaderPresenter(savedState, navigator, userRepo);
    }
}
