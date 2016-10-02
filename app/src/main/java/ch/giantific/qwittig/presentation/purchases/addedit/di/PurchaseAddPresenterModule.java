/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditContract;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddOcrPresenter;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddPresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the purchase add screen and how to instantiate it.
 */
@Module
public class PurchaseAddPresenterModule extends BasePresenterModule {

    public PurchaseAddPresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    PurchaseAddEditContract.Presenter providesPurchaseAddPresenter(@NonNull Navigator navigator,
                                                                   @NonNull UserRepository userRepo,
                                                                   @NonNull GroupRepository groupRepo,
                                                                   @NonNull PurchaseRepository purchaseRepo,
                                                                   @NonNull RemoteConfigHelper configHelper) {
        return new PurchaseAddPresenter(savedState, navigator, userRepo, groupRepo, purchaseRepo,
                configHelper);
    }

    @PerActivity
    @Provides
    PurchaseAddEditContract.AddOcrPresenter providesPurchaseAddOcrPresenter(@NonNull Navigator navigator,
                                                                            @NonNull UserRepository userRepo,
                                                                            @NonNull GroupRepository groupRepo,
                                                                            @NonNull PurchaseRepository purchaseRepo,
                                                                            @NonNull RemoteConfigHelper configHelper) {
        return new PurchaseAddOcrPresenter(savedState, navigator, userRepo, groupRepo,
                purchaseRepo, configHelper);
    }
}
