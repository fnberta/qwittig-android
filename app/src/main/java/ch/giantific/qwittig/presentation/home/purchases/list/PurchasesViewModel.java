/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadMoreViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;

/**
 * Defines an observable view model for list of purchases screen.
 */
public interface PurchasesViewModel extends OnlineListViewModel<Purchase>,
        PurchasesRecyclerAdapter.AdapterInteractionListener,
        PurchasesUpdateWorkerListener, PurchasesQueryMoreWorkerListener, LoadMoreViewModel {

    int TYPE_ITEM = 0;
    int TYPE_PROGRESS = 1;

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdatePurchasesWorker();

        void loadQueryMorePurchasesWorker(int skip);

        void startPurchaseDetailsActivity(@NonNull Purchase purchase);

    }
}
