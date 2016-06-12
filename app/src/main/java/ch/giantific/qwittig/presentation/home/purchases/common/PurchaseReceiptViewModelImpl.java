/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.common;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;

/**
 * Provides an implementation of the {@link PurchaseReceiptViewModel}.
 */
public class PurchaseReceiptViewModelImpl extends ViewModelBaseImpl<PurchaseReceiptViewModel.ViewListener>
        implements PurchaseReceiptViewModel {

    private static final String STATE_LOADING = "STATE_LOADING";
    private boolean mLoading;
    private String mReceiptImageUri;

    public PurchaseReceiptViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull PurchaseReceiptViewModel.ViewListener view,
                                        @NonNull UserRepository userRepository,
                                        @NonNull String receiptImageUri) {
        super(savedState, view, userRepository);

        mReceiptImageUri = receiptImageUri;

        if (savedState != null) {
            mLoading = savedState.getBoolean(STATE_LOADING, false);
        } else {
            mLoading = true;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_LOADING, mLoading);
    }

    @Override
    @Bindable
    public String getReceiptImage() {
        return mReceiptImageUri;
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyPropertyChanged(BR.loading);
    }

    @Override
    public void onReceiptImagePathSet(@NonNull String receiptImageUri) {
        mReceiptImageUri = receiptImageUri;
    }

    @Override
    public void onReceiptImageCaptured() {
        notifyPropertyChanged(BR.receiptImage);
        mView.showMessage(R.string.toast_receipt_changed);
    }
}
