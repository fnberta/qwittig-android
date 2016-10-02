/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.presenters.ListPresenter;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.CompsPaidViewModel;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.items.CompPaidItemViewViewModel;

/**
 * Defines an observable view model for a screen showing a list of paid compensations.
 */
public interface CompsPaidContract {

    interface Presenter extends BasePresenter<ViewListener>,
            ListPresenter<CompPaidItemViewViewModel> {

        CompsPaidViewModel getViewModel();

        void setListInteraction(@NonNull ListInteraction listInteraction);
    }

    interface ViewListener extends BaseViewListener {
        // empty
    }
}
