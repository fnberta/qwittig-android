/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesContract;
import ch.giantific.qwittig.presentation.purchases.list.purchases.PurchasesPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which view model implementation to use for the list of purchases screen and how to
 * instantiate it.
 */
@Module
public class PurchasesPresenterModule extends BasePresenterModule {

    public PurchasesPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    PurchasesContract.Presenter providesHomePurchasesPresenter(@NonNull Navigator navigator,
                                                               @NonNull UserRepository userRepo,
                                                               @NonNull PurchaseRepository purchaseRepo) {
        return new PurchasesPresenter(savedState, navigator, userRepo, purchaseRepo);
    }
}
