/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadMoreViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;
import rx.Observable;

/**
 * Created by fabio on 21.01.16.
 */
public interface PurchasesViewModel extends OnlineListViewModel<Purchase>,
        PurchasesRecyclerAdapter.AdapterInteractionListener,
        PurchasesUpdateWorkerListener, PurchasesQueryMoreWorkerListener, LoadMoreViewModel {

    int TYPE_ITEM = 0;
    int TYPE_PROGRESS = 1;

    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdatePurchasesWorker();

        void loadQueryMorePurchasesWorker(int skip);

        void startPurchaseDetailsActivity(@NonNull Purchase purchase);

    }
}
