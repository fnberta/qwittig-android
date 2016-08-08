/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.di.scopes.PerActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.BaseViewModelModule;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsViewModel;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Defines which implementation of the view model for the purchase details screen to use and how to
 * instantiate it.
 */
@Module
public class PurchaseDetailsViewModelModule extends BaseViewModelModule {

    private final String mPurchaseId;
    private final String mPurchaseGroupId;

    public PurchaseDetailsViewModelModule(@Nullable Bundle savedState, @NonNull String purchaseId,
                                          @Nullable String purchaseGroupId) {
        super(savedState);

        mPurchaseId = purchaseId;
        mPurchaseGroupId = purchaseGroupId;
    }

    @PerActivity
    @Provides
    PurchaseDetailsViewModel providesPurchaseDetailsViewModel(@NonNull Navigator navigator,
                                                              @NonNull RxBus<Object> eventBus,
                                                              @NonNull UserRepository userRepository,
                                                              @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseDetailsViewModelImpl(mSavedState, navigator, eventBus, userRepository,
                purchaseRepository, mPurchaseId, mPurchaseGroupId);
    }
}
