/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseBaseItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseDateRowViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseStoreRowViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseTotalRowViewModel;
import rx.Single;

/**
 * Defines an observable view model for the add or edit purchase screen.
 */
public interface AddEditPurchaseViewModel extends ListViewModel<AddEditPurchaseBaseItem>,
        AddEditPurchaseRecyclerAdapter.AdapterListener, AddEditPurchaseItem.PriceChangedListener,
        AddEditPurchaseDateRowViewModel, AddEditPurchaseStoreRowViewModel,
        AddEditPurchaseTotalRowViewModel, AddEditPurchaseItemUsersClickListener,
        NoteDialogFragment.DialogInteractionListener,
        DiscardChangesDialogFragment.DialogInteractionListener,
        ExchangeRateDialogFragment.DialogInteractionListener, RatesWorkerListener {

    List<String> getSupportedCurrencies();

    NumberFormat getMoneyFormatter();

    void onReceiptImageTaken(@NonNull String receiptImagePath);

    void onReceiptImageTakeFailed();

    void onItemDismiss(int position);

    void onAddReceiptImageMenuClick();

    void onDeleteReceiptImageMenuClick();

    void onShowReceiptImageMenuClick();

    void onAddNoteMenuClick();

    void onShowNoteMenuClick();

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

        void toggleReceiptMenuOption(boolean show);

        void toggleNoteMenuOption(boolean show);

        void showReceiptImage(@NonNull String receiptImageUri);

        void showNote(@NonNull String note);

        void showAddEditNoteDialog(@NonNull String note);

        void captureImage();

        void finishScreen(int purchaseResult);
    }
}
