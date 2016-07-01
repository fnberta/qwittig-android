/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.RemoteConfigRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
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
                                                          @NonNull RemoteConfigRepository configRepository,
                                                          @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseAddViewModelImpl(mSavedState, navigator, eventBus, userRepository,
                configRepository, purchaseRepository);
    }

    @PerActivity
    @Provides
    PurchaseAddOcrViewModel providesPurchaseAddOcrViewModel(@NonNull Navigator navigator,
                                                            @NonNull RxBus<Object> eventBus,
                                                            @NonNull UserRepository userRepository,
                                                            @NonNull RemoteConfigRepository configRepository,
                                                            @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseAddOcrViewModelImpl(mSavedState, navigator, eventBus, userRepository,
                configRepository, purchaseRepository);
    }
}
