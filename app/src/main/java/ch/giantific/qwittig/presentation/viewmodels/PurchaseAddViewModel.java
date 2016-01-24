/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.support.annotation.NonNull;
import android.view.View;

import ch.giantific.qwittig.domain.models.RowItem;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.ui.adapters.PurchaseAddRecyclerAdapter;
import ch.giantific.qwittig.presentation.viewmodels.rows.PurchaseAddCurrencyRowViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.PurchaseAddDateViewModel;
import ch.giantific.qwittig.presentation.viewmodels.rows.PurchaseAddStoreRowViewModel;
import ch.giantific.qwittig.presentation.workerfragments.RatesWorkerListener;

/**
 * Created by fabio on 24.01.16.
 */
public interface PurchaseAddViewModel extends ViewModel<PurchaseAddViewModel.ViewListener>,
        PurchaseAddRecyclerAdapter.AdapterListener, PurchaseAddDateViewModel,
        PurchaseAddStoreRowViewModel, PurchaseAddCurrencyRowViewModel, RatesWorkerListener {

    int TYPE_HEADER = 0;
    int TYPE_DATE = 1;
    int TYPE_STORE = 2;
    int TYPE_ITEM = 3;
    int TYPE_USERS = 4;
    int TYPE_ADD_ROW = 5;
    int TYPE_TOTAL = 6;
    int ROWS_BEFORE_ITEMS = 4;

    RowItem getRowItemAtPosition(int position);

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * <code>position</code>. Type codes need not be contiguous.
     */
    int getItemViewType(int position);

    /**
     * Returns the total number of items in the data set hold by the view model.
     *
     * @return the total number of items in this view model.
     */
    int getItemCount();

    int getLastPosition();

    int getAdjustedPosition(int position);

    void onItemDismissed(int position);

    void onFabSavePurchaseClick(View view);

    void onSavePurchaseAsDraftClick();

    interface ViewListener extends ViewModel.ViewListener {
        String getReceiptImage();

        void loadFetchExchangeRatesWorker();

        void loadSavePurchaseWorker(@NonNull Purchase purchase, @NonNull String receiptImagePath);

        void showDatePickerDialog();

        void notifyItemRemoved(int position);

        void notifyItemInserted(int position);

        void scrollToPosition(int position);
    }
}
