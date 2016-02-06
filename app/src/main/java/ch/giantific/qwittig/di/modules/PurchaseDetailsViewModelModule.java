/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.di.scopes.PerFragment;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsViewModel;
import ch.giantific.qwittig.presentation.home.purchases.details.PurchaseDetailsViewModelImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by fabio on 12.01.16.
 */
@Module
public class PurchaseDetailsViewModelModule extends BaseViewModelModule<PurchaseDetailsViewModel.ViewListener> {

    private String mPurchaseId;

    public PurchaseDetailsViewModelModule(@Nullable Bundle savedState,
                                          @NonNull PurchaseDetailsViewModel.ViewListener view,
                                          @NonNull String purchaseId) {
        super(savedState, view);
        mPurchaseId = purchaseId;
    }

    @PerFragment
    @Provides
    PurchaseDetailsViewModel providesPurchaseDetailsViewModel(@NonNull UserRepository userRepository,
                                                              @NonNull GroupRepository groupRepository,
                                                              @NonNull PurchaseRepository purchaseRepository) {
        return new PurchaseDetailsViewModelImpl(mSavedState, mView, groupRepository, userRepository,
                purchaseRepository, mPurchaseId);
    }
}
