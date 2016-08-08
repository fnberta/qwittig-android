/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddOcrViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddOcrViewModelImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the purchase add screen and how to instantiate it.
 */
@Module
public class PurchaseAddViewModelModule extends BaseViewModelModule {

    public PurchaseAddViewModelModule(@Nullable Bundle savedState) {
        super(savedState);
    }

    @PerActivity
    @Provides
    PurchaseAddEditViewModel providesPurchaseAddViewModel(@NonNull Navigator navigator,
                                                          @NonNull RxBus<Object> eventBus,
                                                          @NonNull UserRepository userRepository,
                                                          @NonNull PurchaseRepository purchaseRepository,
                                                          @NonNull RemoteConfigHelper configHelper) {
        return new PurchaseAddViewModelImpl(mSavedState, navigator, eventBus, userRepository,
                purchaseRepository, configHelper);
    }

    @PerActivity
    @Provides
    PurchaseAddOcrViewModel providesPurchaseAddOcrViewModel(@NonNull Navigator navigator,
                                                            @NonNull RxBus<Object> eventBus,
                                                            @NonNull UserRepository userRepository,
                                                            @NonNull PurchaseRepository purchaseRepository,
                                                            @NonNull RemoteConfigHelper configHelper) {
        return new PurchaseAddOcrViewModelImpl(mSavedState, navigator, eventBus, userRepository,
                purchaseRepository, configHelper);
    }
}
