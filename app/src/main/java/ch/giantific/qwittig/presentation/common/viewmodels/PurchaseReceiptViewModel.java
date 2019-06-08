/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;

/**
 * Defines an observable view model for the purchase receipt screen.
 */
public interface PurchaseReceiptViewModel extends LoadingViewModel {

    @Bindable
    String getReceipt();

    void setReceipt(@NonNull String receiptUrl);

    @Bindable
    boolean isReceiptAvailable();
}
