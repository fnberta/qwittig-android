/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.parse.ParseFile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import ch.berta.fabio.fabprogress.ProgressFinalAnimationListener;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditBaseItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditDateRowViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditStoreRowViewModel;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditTotalRowViewModel;
import rx.Single;

/**
 * Defines an observable view model for the add or edit purchase screen.
 */
public interface PurchaseAddEditViewModel extends ListViewModel<PurchaseAddEditBaseItem>,
        PurchaseAddEditRecyclerAdapter.AdapterListener, PurchaseAddEditItem.PriceChangedListener,
        PurchaseAddEditDateRowViewModel, PurchaseAddEditStoreRowViewModel,
        PurchaseAddEditTotalRowViewModel, PurchaseAddEditItemUsersClickListener,
        PurchaseNoteDialogFragment.DialogInteractionListener,
        DiscardChangesDialogFragment.DialogInteractionListener, OcrWorkerListener,
        PurchaseExchangeRateDialogFragment.DialogInteractionListener, RatesWorkerListener,
        PurchaseSaveWorkerListener, ProgressFinalAnimationListener {

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
    void onAddReceiptImageClick();

    void onDeleteReceiptImageClick();

    void onShowReceiptImageClick();

    void onAddNoteClick();

    void onShowNoteClick();

    void onEditNoteClick();

    /**
     * Sets the note field to an empty string. When the user saves the purchase it will get deleted
     * in the online database.
     */
    void onDeleteNoteClick();

    void onUpOrBackClick();

    /**
     * Saves the purchase to the online and offline local data store.
     *
     * @param view the fab that was clicked
     */
    void onFabSavePurchaseClick(View view);

    /**
     * Saves the purchase as a draft to the local data store.
     */
    void onSavePurchaseAsDraftClick();

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

        void loadSavePurchaseWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage);

        void loadSavePurchaseWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage,
                                    @Nullable ParseFile receiptParseFileOld,
                                    boolean deleteOldReceipt, boolean draft);

        void showDatePickerDialog();

        void showManualExchangeRateSelectorDialog(@NonNull String exchangeRate);

        void showPurchaseDiscardDialog();

        void showDiscardEditChangesDialog();

        void toggleReceiptMenuOption(boolean show);

        void toggleNoteMenuOption(boolean show);

        void showReceiptImage(@NonNull String receiptImagePath);

        void showReceiptImage(@NonNull String objectId, @NonNull String receiptImagePath,
                              boolean isDraft);

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

        void startSaveAnim();

        void stopSaveAnim();

        void showSaveFinishedAnim();
    }
}
