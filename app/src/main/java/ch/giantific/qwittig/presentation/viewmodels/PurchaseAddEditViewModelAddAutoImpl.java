/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;

/**
 * Created by fabio on 28.01.16.
 */
public class PurchaseAddEditViewModelAddAutoImpl extends PurchaseAddEditViewModelAddImpl {

    public PurchaseAddEditViewModelAddAutoImpl(@Nullable Bundle savedState,
                                               @NonNull GroupRepository groupRepository,
                                               @NonNull UserRepository userRepository,
                                               @NonNull SharedPreferences sharedPreferences,
                                               @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, groupRepository, userRepository, sharedPreferences, purchaseRepo);

        if (savedState == null) {
            mLoading = true;
        }
    }

    @Override
    public void setLoading(boolean loading) {
        super.setLoading(loading);

        mView.reloadOptionsMenu();
    }

    @Override
    void onUsersReady() {
        mView.captureImage(USE_CUSTOM_CAMERA);
    }

    @Override
    public void onReceiptImageTaken() {
        mView.loadOcrWorker(mReceiptImagePath);
    }
}
