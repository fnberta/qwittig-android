/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.common;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the purchase receipt screen.
 */
public interface PurchaseReceiptViewModel extends ViewModel,
        LoadingViewModel {

    void onReceiptImagePathSet(@NonNull String receiptImagePath);

    void onReceiptImageCaptured();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

        void setReceiptImage(@NonNull String receiptImagePath);

        void setReceiptImage(@NonNull byte[] receiptImage);
    }
}
