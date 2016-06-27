/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadMoreViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;

/**
 * Defines an observable view model for a screen showing a list of paid compensations.
 */
public interface CompsPaidViewModel extends OnlineListViewModel<Compensation, CompsPaidViewModel.ViewListener>,
        CompsQueryMoreWorkerListener, LoadMoreViewModel {

    int TYPE_ITEM = 0;
    int TYPE_PROGRESS = 1;

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends OnlineListViewModel.ViewListener {

        void startUpdateCompensationsPaidService();

        void loadQueryMoreCompensationsPaidWorker(int skip);
    }
}
