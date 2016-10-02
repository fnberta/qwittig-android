/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BasePresenterModule;
import ch.giantific.qwittig.presentation.purchases.list.HomeContract;
import ch.giantific.qwittig.presentation.purchases.list.HomePresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the home view model and how to instantiate it.
 */
@Module
public class HomePresenterModule extends BasePresenterModule {

    public HomePresenterModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    HomeContract.Presenter providesHomePresenter(@NonNull Navigator navigator,
                                                 @NonNull UserRepository userRepo,
                                                 @NonNull GroupRepository groupRepo,
                                                 @NonNull PurchaseRepository purchaseRepo) {
        return new HomePresenter(savedState, navigator, userRepo, groupRepo, purchaseRepo);
    }
}
