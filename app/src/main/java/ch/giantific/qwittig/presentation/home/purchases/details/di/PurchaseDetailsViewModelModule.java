/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsViewModel;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation of the view model for the purchase details screen to use and how to
 * instantiate it.
 */
@Module
public class PurchaseDetailsViewModelModule extends BaseViewModelModule<PurchaseDetailsViewModel.ViewListener> {

    private final String mPurchaseId;

    public PurchaseDetailsViewModelModule(@Nullable Bundle savedState,
                                          @NonNull PurchaseDetailsViewModel.ViewListener view,
                                          @NonNull String purchaseId) {
        super(savedState, view);
        mPurchaseId = purchaseId;
    }

    @PerScreen
    @Provides
    PurchaseDetailsViewModel providesPurchaseDetailsViewModel(@NonNull RxBus<Object> eventBus,
                                                              @NonNull UserRepository userRepository,
                                                              @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseDetailsViewModelImpl(mSavedState, mView, eventBus, userRepository,
                purchaseRepository, mPurchaseId);
    }
}
