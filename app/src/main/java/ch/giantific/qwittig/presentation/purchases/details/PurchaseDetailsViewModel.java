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

    @Bindable
    String getPurchaseStore();

    @Bindable
    String getPurchaseDate();

    Identity getPurchaseBuyer();

    void onEditPurchaseClick();

    void onDeletePurchaseClick();

    void onShowExchangeRateClick();

    @IntDef({PurchaseDetailsResult.PURCHASE_DELETED})
    @Retention(RetentionPolicy.SOURCE)
    @interface PurchaseDetailsResult {
        int PURCHASE_DELETED = 2;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {

        void startEnterTransition();

        void toggleMenuOptions(boolean showEditOptions, boolean hasForeignCurrency);
    }
}
