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
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidContract;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the paid compensations view model and how to instantiate
 * it.
 */
@Module
public class FinanceCompsPaidPresenterModule extends BasePresenterModule {

    private final String compGroupId;

    public FinanceCompsPaidPresenterModule(@Nullable Bundle savedState,
                                           @Nullable String compGroupId) {
        super(savedState);

        this.compGroupId = compGroupId;
    }

    @PerActivity
    @Provides
    CompsPaidContract.Presenter providesFinanceCompsPaidPresenter(@NonNull Navigator navigator,
                                                                  @NonNull UserRepository userRepo,
                                                                  @NonNull CompensationRepository compsRepo) {
        return new CompsPaidPresenter(savedState, navigator, userRepo, compsRepo, compGroupId);
    }
}
