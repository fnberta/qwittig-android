/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.common.presenters.SortedListPresenter;
import ch.giantific.qwittig.presentation.common.views.SortedListView;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.items.CompPaidItemViewModel;

/**
 * Defines an observable view model for a screen showing a list of paid compensations.
 */
public interface CompsPaidContract {

    interface Presenter extends BasePresenter<ViewListener>,
            SortedListPresenter<CompPaidItemViewModel> {
        // empty
    }

    interface ViewListener extends BaseView,
            SortedListView<CompPaidItemViewModel> {
        // empty
    }
}
