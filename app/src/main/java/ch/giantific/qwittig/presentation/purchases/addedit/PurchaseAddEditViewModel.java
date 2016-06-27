/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.app.Activity;
import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.List;

import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditDateItemModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemUsersUser;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditStoreItemModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditTotalItemModel;

/**
 * Defines an observable view model for the add or edit purchase screen.
 */
public interface PurchaseAddEditViewModel extends ListViewModel<PurchaseAddEditItemModel, PurchaseAddEditViewModel.ViewListener>,
        PurchaseAddEditItem.PriceChangedListener,
        PurchaseAddEditDateItemModel, PurchaseAddEditStoreItemModel,
        PurchaseAddEditTotalItemModel,
        NoteDialogFragment.DialogInteractionListener,
        DiscardChangesDialogFragment.DialogInteractionListener,
        ExchangeRateDialogFragment.DialogInteractionListener, RatesWorkerListener,
        PurchaseReceiptViewModel {

    void setReceiptImage(@NonNull String receiptImagePath);

    @Bindable
    String getNote();

    void setNote(@NonNull String note);

    boolean isNoteAvailable();

    List<String> getSupportedCurrencies();

    NumberFormat getMoneyFormatter();

    void onReceiptImageTaken(@NonNull String receiptImagePath);

    void onReceiptImageTakeFailed();

    void onAddEditReceiptImageMenuClick();

    void onDeleteReceiptMenuClick();

    void onAddEditNoteMenuClick();

    void onToggleUsersClick(@NonNull PurchaseAddEditItem itemModel);

    void onAddRowClick(@NonNull PurchaseAddEditItemModel itemModel);

    void onItemDismiss(int position);

    void onItemRowUserClick();

    void onItemRowUserLongClick(@NonNull PurchaseAddEditItemUsersUser userClicked);

    void onExitClick();

    void onFabSavePurchaseClick(View view);

    void onSaveAsDraftMenuClick();

    @IntDef({PurchaseResult.PURCHASE_SAVED, PurchaseResult.PURCHASE_SAVED_AUTO,
            PurchaseResult.PURCHASE_DRAFT, PurchaseResult.PURCHASE_DRAFT_CHANGES,
            PurchaseResult.PURCHASE_DISCARDED, PurchaseResult.PURCHASE_DRAFT_DELETED,
            Activity.RESULT_CANCELED})
    @Retention(RetentionPolicy.SOURCE)
    @interface PurchaseResult {
        int PURCHASE_SAVED = 2;
        int PURCHASE_SAVED_AUTO = 3;
        int PURCHASE_DRAFT = 4;
        int PURCHASE_DRAFT_CHANGES = 5;
        int PURCHASE_DISCARDED = 6;
        int PURCHASE_DRAFT_DELETED = 7;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
        void loadFetchExchangeRatesWorker(@NonNull String baseCurrency, @NonNull String currency);

        void showDatePickerDialog();

        void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate);

        void showPurchaseDiscardDialog();

        void showDiscardEditChangesDialog();

        void showAddEditNoteDialog(@NonNull String note);

        void captureImage();

        void showPurchaseItems();

        void reloadOptionsMenu();
    }
}
