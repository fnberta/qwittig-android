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
import ch.giantific.qwittig.presentation.purchases.addedit.items.BasePurchaseAddEditItem;
import ch.giantific.qwittig.presentation.purchases.addedit.items.PurchaseAddEditDateRowViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.items.PurchaseAddEditItem;
import ch.giantific.qwittig.presentation.purchases.addedit.items.PurchaseAddEditStoreRowViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.items.PurchaseAddEditTotalRowViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.PurchaseReceiptViewModel;

/**
 * Defines an observable view model for the add or edit purchase screen.
 */
public interface PurchaseAddEditViewModel extends ListViewModel<BasePurchaseAddEditItem, PurchaseAddEditViewModel.ViewListener>,
        PurchaseAddEditRecyclerAdapter.AdapterListener, PurchaseAddEditItem.PriceChangedListener,
        PurchaseAddEditDateRowViewModel, PurchaseAddEditStoreRowViewModel,
        PurchaseAddEditTotalRowViewModel, PurchaseAddEditItemUsersClickListener,
        NoteDialogFragment.DialogInteractionListener,
        DiscardChangesDialogFragment.DialogInteractionListener,
        ExchangeRateDialogFragment.DialogInteractionListener, RatesWorkerListener,
        PurchaseReceiptViewModel {

    void setReceiptOrNoteShown(boolean receiptOrNoteShown);

    void setReceiptImage(@NonNull String receiptImagePath);

    @Bindable
    String getNote();

    void setNote(@NonNull String note);

    List<String> getSupportedCurrencies();

    NumberFormat getMoneyFormatter();

    void onReceiptImageTaken(@NonNull String receiptImagePath);

    void onReceiptImageTakeFailed();

    void onAddReceiptImageMenuClick();

    void onShowReceiptImageMenuClick();

    void onEditReceiptMenuClick();

    void onDeleteReceiptMenuClick();

    void onAddNoteMenuClick();

    void onShowNoteMenuClick();

    void onEditNoteMenuClick();

    void onDeleteNoteMenuClick();

    void onItemDismiss(int position);

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

        void showPurchaseScreen();
    }
}
