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
 * Created by fabio on 28.01.16.
 */
public class PurchaseAddEditViewModelAddAutoImpl extends PurchaseAddEditViewModelAddImpl {

    public PurchaseAddEditViewModelAddAutoImpl(@Nullable Bundle savedState,
                                               @NonNull PurchaseAddEditViewModel.ViewListener view,
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
