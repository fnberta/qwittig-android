/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.common;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Created by fabio on 29.01.16.
 */
public interface PurchaseReceiptViewModel extends ViewModel,
        LoadingViewModel {

    void onReceiptImagePathSet(@NonNull String receiptImagePath);

    void onReceiptImageCaptured();

    interface ViewListener extends ViewModel.ViewListener {

        void setReceiptImage(@NonNull String receiptImagePath);

        void setReceiptImage(@NonNull byte[] receiptImage);
    }
}
