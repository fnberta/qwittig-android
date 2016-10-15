/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.common.presenters.SortedListPresenter;
import ch.giantific.qwittig.presentation.common.views.SortedListView;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.items.PurchaseItemViewModel;

/**
 * Defines an observable view model for list of purchases screen.
 */
public interface PurchasesContract {

    interface Presenter extends BasePresenter<ViewListener>,
            SortedListPresenter<PurchaseItemViewModel> {

        void onPurchaseRowItemClick(@NonNull PurchaseItemViewModel itemViewModel);

        void onPurchaseDeleted(@NonNull String purchaseId);
    }

    interface ViewListener extends BaseView,
            SortedListView<PurchaseItemViewModel> {
        // empty
    }
}
