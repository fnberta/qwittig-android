/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsContract;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation of the view model for the purchase details screen to use and how to
 * instantiate it.
 */
@Module
public class PurchaseDetailsPresenterModule extends BasePresenterModule {

    private final String purchaseId;
    private final String purchaseGroupId;

    public PurchaseDetailsPresenterModule(@Nullable Bundle savedState, @NonNull String purchaseId,
                                          @Nullable String purchaseGroupId) {
        super(savedState);

        this.purchaseId = purchaseId;
        this.purchaseGroupId = purchaseGroupId;
    }

    @PerActivity
    @Provides
    PurchaseDetailsContract.Presenter providesPurchaseDetailsPresenter(@NonNull Navigator navigator,
                                                                       @NonNull UserRepository userRepo,
                                                                       @NonNull PurchaseRepository purchaseRepo) {
        return new PurchaseDetailsPresenter(savedState, navigator, userRepo, purchaseRepo,
                purchaseId, purchaseGroupId);
    }
}
