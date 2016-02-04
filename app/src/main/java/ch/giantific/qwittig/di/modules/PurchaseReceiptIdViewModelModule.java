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
public class PurchaseReceiptIdViewModelModule extends BaseViewModelModule<PurchaseReceiptViewModel.ViewListener> {

    private String mPurchaseId;
    private boolean mDraft;

    public PurchaseReceiptIdViewModelModule(@Nullable Bundle savedState,
                                            @NonNull PurchaseReceiptViewModel.ViewListener view,
                                            @NonNull String purchaseId, boolean draft) {
        super(savedState, view);

        mPurchaseId = purchaseId;
        mDraft = draft;
    }

    @PerFragment
    @Provides
    PurchaseReceiptViewModel providesPurchaseReceiptIdViewModel(@NonNull UserRepository userRepository,
                                                                @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseReceiptViewModelImpl(mSavedState, mView, userRepository,
                purchaseRepository, mPurchaseId, mDraft);
    }
}
