/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Provides an implementation of the {@link AddEditPurchaseViewModel} for the screen where the
 * purchase items get automatically filled in via an image of the receipt.
 * <p/>
 * Subclass of {@link AddPurchaseViewModelImpl}.
 */
public class AddPurchaseViewModelAutoImpl extends AddPurchaseViewModelImpl {

    public AddPurchaseViewModelAutoImpl(@Nullable Bundle savedState,
                                        @NonNull AddEditPurchaseViewModel.ViewListener view,
                                        @NonNull IdentityRepository identityRepository,
                                        @NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, view, identityRepository, userRepository, purchaseRepo);

        if (savedState == null) {
            mLoading = true;
        }
    }

    @Override
    public void setLoading(boolean loading) {
        super.setLoading(loading);

        if (!loading) {
            mView.showOptionsMenu();
        }
    }

    @Override
    void onIdentitiesReady() {
        mView.captureImage(USE_CUSTOM_CAMERA);
    }

    @Override
    public void onReceiptImageTaken() {
        mView.toggleReceiptMenuOption(true);
        mView.loadOcrWorker(mReceiptImagePath);
    }
}
