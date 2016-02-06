/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;

/**
 * Created by fabio on 29.01.16.
 */
public interface PurchaseDetailsViewModel extends ListViewModel<PurchaseDetailsItem> {

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

    interface ViewListener extends ListViewModel.ViewListener {

        /**
         * Starts the postponed enter transition.
         */
        void startPostponedEnterTransition();

        void startPurchaseEditScreen(@NonNull String purchaseId);

        void showReceiptImage(@NonNull String purchaseId);

        void toggleMenuOptions(boolean showEditOptions, boolean hasReceiptImage,
                               boolean hasForeignCurrency);

        void finishScreen(int result);
    }
}
