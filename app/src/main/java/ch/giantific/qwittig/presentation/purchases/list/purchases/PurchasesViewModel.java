/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadMoreViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.itemmodels.PurchasesItemModel;

/**
 * Defines an observable view model for list of purchases screen.
 */
public interface PurchasesViewModel extends OnlineListViewModel<Purchase, PurchasesViewModel.ViewListener>,
        PurchasesQueryMoreWorkerListener, LoadMoreViewModel {

    int TYPE_ITEM = 0;
    int TYPE_PROGRESS = 1;

    void onPurchaseRowItemClick(@NonNull Purchase purchase);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends OnlineListViewModel.ViewListener {

        void startUpdatePurchasesService();

        void loadQueryMorePurchasesWorker(int skip);
    }
}
