/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.itemmodels.PurchaseItemModel;

/**
 * Defines an observable view model for list of purchases screen.
 */
public interface PurchasesViewModel extends ListViewModel<PurchaseItemModel, PurchasesViewModel.ViewListener> {

    void onPurchaseRowItemClick(@NonNull PurchaseItemModel itemModel);

    void onPurchaseDeleted(@NonNull String purchaseId);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
    }
}
