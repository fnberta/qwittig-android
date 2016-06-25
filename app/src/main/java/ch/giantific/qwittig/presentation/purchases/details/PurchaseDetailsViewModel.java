/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.databinding.Bindable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.purchases.details.itemmodels.PurchaseDetailsItemModel;

/**
 * Defines an observable view model for the purchase details screen.
 */
public interface PurchaseDetailsViewModel extends ListViewModel<PurchaseDetailsItemModel, PurchaseDetailsViewModel.ViewListener>, PurchaseReceiptViewModel {

    void setReceiptShown(boolean receiptShown);

    @Bindable
    String getPurchaseStore();

    @Bindable
    String getPurchaseDate();

    Identity getPurchaseBuyer();

    void onEditPurchaseClick();

    void onDeletePurchaseClick();

    void onShowExchangeRateClick();

    void onShowReceiptImageClick();

    @IntDef({PurchaseDetailsResult.PURCHASE_DELETED, PurchaseDetailsResult.GROUP_CHANGED})
    @Retention(RetentionPolicy.SOURCE)
    @interface PurchaseDetailsResult {
        int PURCHASE_DELETED = 2;
        int GROUP_CHANGED = 3;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {

        void startEnterTransition();

        void showPurchaseDetailsReceipt();

        void toggleMenuOptions(boolean showEditOptions, boolean hasReceiptImage,
                               boolean hasForeignCurrency);
    }
}
