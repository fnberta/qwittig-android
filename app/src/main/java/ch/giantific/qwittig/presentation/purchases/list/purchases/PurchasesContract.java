/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.presenters.ListPresenter;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.PurchasesViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.items.PurchaseItemViewModel;

/**
 * Defines an observable view model for list of purchases screen.
 */
public interface PurchasesContract {

    interface Presenter extends BasePresenter<ViewListener>,
            ListPresenter<PurchaseItemViewModel> {

        PurchasesViewModel getViewModel();

        void setListInteraction(@NonNull ListInteraction listInteraction);

        void onPurchaseRowItemClick(@NonNull PurchaseItemViewModel itemViewModel);

        void onPurchaseDeleted(@NonNull String purchaseId);
    }

    interface ViewListener extends BaseViewListener {
        // empty
    }
}
