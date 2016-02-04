/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.support.annotation.NonNull;
import android.view.View;

import com.mugen.MugenCallbacks;

import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.presentation.ui.adapters.PurchasesRecyclerAdapter;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesQueryMoreListener;
import ch.giantific.qwittig.presentation.workerfragments.query.PurchasesUpdateListener;

/**
 * Created by fabio on 21.01.16.
 */
public interface HomePurchasesViewModel extends OnlineListViewModel<Purchase>,
        PurchasesRecyclerAdapter.AdapterInteractionListener,
        PurchasesUpdateListener, PurchasesQueryMoreListener, LoadMoreViewModel {

    int TYPE_ITEM = 0;
    int TYPE_PROGRESS = 1;

    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdatePurchasesWorker();

        void loadQueryMorePurchasesWorker(int skip);

        void startPurchaseDetailsActivity(@NonNull Purchase purchase);

    }
}
