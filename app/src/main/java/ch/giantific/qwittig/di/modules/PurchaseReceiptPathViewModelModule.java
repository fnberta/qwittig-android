/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.purchases.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.home.purchases.PurchaseReceiptViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class PurchaseReceiptPathViewModelModule extends BaseViewModelModule<PurchaseReceiptViewModel.ViewListener> {

    private final String mReceiptImagePath;

    public PurchaseReceiptPathViewModelModule(@Nullable Bundle savedState,
                                              @NonNull PurchaseReceiptViewModel.ViewListener view,
                                              @NonNull String receiptImagePath) {
        super(savedState, view);

        mReceiptImagePath = receiptImagePath;
    }

    @PerFragment
    @Provides
    PurchaseReceiptViewModel providesPurchaseReceiptViewModel(@NonNull UserRepository userRepository,
                                                              @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseReceiptViewModelImpl(mSavedState, mView, userRepository,
                purchaseRepository, mReceiptImagePath);
    }
}
