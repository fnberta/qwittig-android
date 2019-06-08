/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.di;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.CompensationRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidContract;
import ch.giantific.qwittig.presentation.finance.paid.CompsPaidPresenter;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.CompsPaidViewModel;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the paid compensations view model and how to instantiate
 * it.
 */
@Module
public class FinanceCompsPaidPresenterModule {

    private final String compGroupId;

    public FinanceCompsPaidPresenterModule(@Nullable String compGroupId) {
        this.compGroupId = compGroupId;
    }

    @PerActivity
    @Provides
    CompsPaidContract.Presenter providesFinanceCompsPaidPresenter(@NonNull Navigator navigator,
                                                                  @NonNull CompsPaidViewModel viewModel,
                                                                  @NonNull UserRepository userRepo,
                                                                  @NonNull CompensationRepository compsRepo) {
        return new CompsPaidPresenter(navigator, viewModel, userRepo, compsRepo, compGroupId);
    }
}
