/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.di.scopes.PerScreen;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddEditPurchaseViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.AddPurchaseViewModelOcrImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation to use for the purchase add auto screen and how to instantiate it.
 */
@Module
public class AddPurchaseOcrViewModelModule extends BaseViewModelModule<AddEditPurchaseViewModel.ViewListener> {

    private String mOcrPurchaseId;

    public AddPurchaseOcrViewModelModule(@Nullable Bundle savedState,
                                         @NonNull AddEditPurchaseViewModel.ViewListener view,
                                         @NonNull String ocrPurchaseId) {
        super(savedState, view);

        mOcrPurchaseId = ocrPurchaseId;
    }

    @PerScreen
    @Provides
    AddEditPurchaseViewModel providesAddPurchaseOcrViewModel(@NonNull RxBus<Object> eventBus,
                                                             @NonNull UserRepository userRepository,
                                                             @NonNull PurchaseRepository purchaseRepository) {
        return new AddPurchaseViewModelOcrImpl(mSavedState, mView, eventBus, userRepository,
                purchaseRepository, mOcrPurchaseId);
    }
}
