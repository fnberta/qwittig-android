/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.PurchaseDetailsItem;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Created by fabio on 29.01.16.
 */
public interface PurchaseDetailsViewModel extends ListViewModel<PurchaseDetailsItem, PurchaseDetailsViewModel.ViewListener> {

    int RESULT_PURCHASE_DELETED = 2;
    int RESULT_GROUP_CHANGED = 3;

    @Bindable
    String getPurchaseStore();

    @Bindable
    String getPurchaseDate();

    User getPurchaseBuyer();

    void onEditPurchaseClick();

    void onDeletePurchaseClick();

    void onShowExchangeRateClick();

    void onShowReceiptImageClick();

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
