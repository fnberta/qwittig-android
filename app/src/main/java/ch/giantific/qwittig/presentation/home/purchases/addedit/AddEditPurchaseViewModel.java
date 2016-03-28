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
        DiscardChangesDialogFragment.DialogInteractionListener, OcrWorkerListener,
        ExchangeRateDialogFragment.DialogInteractionListener, RatesWorkerListener {

    void onDateSet(@NonNull Date date);

    /**
     * Sets the receipt image path field.
     *
     * @param receiptImagePath the path to the receipt image
     */
    void onReceiptImagePathSet(@NonNull String receiptImagePath);

    void onReceiptImageTaken();

    void onReceiptImageFailed();

    /**
     * Sets the receipt image paths, only used when custom camera is enabled
     *
     * @param receiptImagePaths the paths of the receipt images
     */
    void onReceiptImagesTaken(@NonNull List<String> receiptImagePaths);

    void onItemDismiss(int position);

    /**
     * Launches the camera that allows the user to add a receipt image to the purchase.
     */
    void onAddReceiptImageMenuClick();

    void onDeleteReceiptImageMenuClick();

    void onShowReceiptImageMenuClick();

    void onAddNoteMenuClick();

    void onShowNoteMenuClick();

    void onExitClick();

    /**
     * Saves the purchase to the online and offline local data store.
     *
     * @param view the fab that was clicked
     */
    void onFabSavePurchaseClick(View view);

    /**
     * Saves the purchase as a draft to the local data store.
     */
    void onSaveAsDraftMenuClick();

    List<String> getSupportedCurrencies();

    NumberFormat getMoneyFormatter();

    @IntDef({PurchaseResult.PURCHASE_SAVED, PurchaseResult.PURCHASE_SAVED_AUTO,
            PurchaseResult.PURCHASE_DRAFT, PurchaseResult.PURCHASE_DRAFT_CHANGES,
            PurchaseResult.PURCHASE_ERROR, PurchaseResult.PURCHASE_DISCARDED,
            PurchaseResult.PURCHASE_DRAFT_DELETED,
            Activity.RESULT_CANCELED})
    @Retention(RetentionPolicy.SOURCE)
    @interface PurchaseResult {
        int PURCHASE_SAVED = 2;
        int PURCHASE_SAVED_AUTO = 3;
        int PURCHASE_DRAFT = 4;
        int PURCHASE_DRAFT_CHANGES = 5;
        int PURCHASE_ERROR = 6;
        int PURCHASE_DISCARDED = 7;
        int PURCHASE_DRAFT_DELETED = 8;
    }

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
        Single<byte[]> encodeReceiptImage(@NonNull String imagePath);

        void loadFetchExchangeRatesWorker(@NonNull String baseCurrency, @NonNull String currency);

        void loadOcrWorker(@NonNull String receiptImagePath);

        void showDatePickerDialog();

        void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate);

        void showPurchaseDiscardDialog();

        void showDiscardEditChangesDialog();

        void toggleReceiptMenuOption(boolean show);

        void toggleNoteMenuOption(boolean show);

        void showReceiptImage(@NonNull String receiptImagePath);

        void showReceiptImage(@NonNull String objectId, @NonNull String receiptImagePath);

        void showNote(@NonNull String note);

        /**
         * Opens a dialog that allows the user to add or edit the note.
         */
        void showAddEditNoteDialog(@NonNull String note);

        /**
         * Checks whether the permissions to take an image are granted and if yes initiates the creation
         * of the image file.
         */
        void captureImage(boolean useCustomCamera);

        void showOptionsMenu();

        void finishScreen(int purchaseResult);
    }
}
