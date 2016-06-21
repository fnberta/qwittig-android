/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadMoreViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;

/**
 * Defines an observable view model for list of purchases screen.
 */
public interface PurchasesViewModel extends OnlineListViewModel<Purchase, PurchasesViewModel.ViewListener>,
        PurchasesRecyclerAdapter.AdapterInteractionListener,
        PurchasesQueryMoreWorkerListener, LoadMoreViewModel {

    int TYPE_ITEM = 0;
    int TYPE_PROGRESS = 1;

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends OnlineListViewModel.ViewListener {

        void startUpdatePurchasesService();

        void loadQueryMorePurchasesWorker(int skip);
    }
}
