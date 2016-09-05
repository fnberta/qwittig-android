/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.finance.paid.itemmodels.CompPaidItemModel;

/**
 * Defines an observable view model for a screen showing a list of paid compensations.
 */
public interface CompsPaidViewModel extends ListViewModel<CompPaidItemModel, CompsPaidViewModel.ViewListener> {

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ListViewModel.ViewListener {
    }
}
