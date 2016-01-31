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
import ch.giantific.qwittig.presentation.viewmodels.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.viewmodels.PurchaseReceiptViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class PurchaseReceiptPathViewModelModule extends BaseViewModelModule {

    private String mReceiptImagePath;

    public PurchaseReceiptPathViewModelModule(@Nullable Bundle savedState, String receiptImagePath) {
        super(savedState);

        mReceiptImagePath = receiptImagePath;
    }

    @PerFragment
    @Provides
    PurchaseReceiptViewModel providesPurchaseReceiptViewModel(@NonNull UserRepository userRepository,
                                                                  @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseReceiptViewModelImpl(mSavedState, userRepository, purchaseRepository, mReceiptImagePath);
    }
}
