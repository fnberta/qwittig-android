/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.common.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.common.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.home.purchases.common.PurchaseReceiptViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation of the view model for the purchase receipt screen to use and how to
 * instantiate it.
 */
@Module
public class PurchaseReceiptViewModelModule extends BaseViewModelModule<PurchaseReceiptViewModel.ViewListener> {

    private final String mReceiptImagePath;

    public PurchaseReceiptViewModelModule(@Nullable Bundle savedState,
                                          @NonNull PurchaseReceiptViewModel.ViewListener view,
                                          @NonNull String receiptImagePath) {
        super(savedState, view);

        mReceiptImagePath = receiptImagePath;
    }

    @PerScreen
    @Provides
    PurchaseReceiptViewModel providesPurchaseReceiptViewModel(@NonNull RxBus<Object> eventBus,
                                                              @NonNull UserRepository userRepository) {
        return new PurchaseReceiptViewModelImpl(mSavedState, mView, eventBus, userRepository, mReceiptImagePath);
    }
}
