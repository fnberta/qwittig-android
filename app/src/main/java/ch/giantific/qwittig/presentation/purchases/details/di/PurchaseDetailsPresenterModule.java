/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.di;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsContract;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsPresenter;
import ch.giantific.qwittig.presentation.purchases.details.viewmodels.PurchaseDetailsViewModel;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation of the view model for the purchase details screen to use and how to
 * instantiate it.
 */
@Module
public class PurchaseDetailsPresenterModule {

    private final String purchaseId;
    private final String purchaseGroupId;

    public PurchaseDetailsPresenterModule(@NonNull String purchaseId,
                                          @Nullable String purchaseGroupId) {
        this.purchaseId = purchaseId;
        this.purchaseGroupId = purchaseGroupId;
    }

    @PerActivity
    @Provides
    PurchaseDetailsContract.Presenter providesPurchaseDetailsPresenter(@NonNull Navigator navigator,
                                                                       @NonNull PurchaseDetailsViewModel viewModel,
                                                                       @NonNull UserRepository userRepo,
                                                                       @NonNull PurchaseRepository purchaseRepo) {
        return new PurchaseDetailsPresenter(navigator, viewModel, userRepo, purchaseRepo,
                purchaseId, purchaseGroupId);
    }
}
