/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.common;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines an observable view model for the purchase receipt screen.
 */
public interface PurchaseReceiptViewModel extends ViewModel,
        LoadingViewModel {

    @Bindable
    String getReceiptImage();

    void setReceiptImage(@NonNull String receiptImagePath);

    void onEditReceiptMenuClick();

    void onReceiptImageTaken(@NonNull String receiptImagePath);

    void onDeleteReceiptMenuClick();

    interface ViewListener extends ViewModel.ViewListener {

        void captureImage();

        void showPurchaseScreen();
    }
}
