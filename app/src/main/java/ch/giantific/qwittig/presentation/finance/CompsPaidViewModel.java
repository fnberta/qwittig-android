/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadMoreViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;

/**
 * Created by fabio on 18.01.16.
 */
public interface CompsPaidViewModel extends OnlineListViewModel<Compensation>,
        CompsUpdateWorkerListener, CompsQueryMoreWorkerListener, LoadMoreViewModel {


    int TYPE_ITEM = 0;
    int TYPE_PROGRESS = 1;

    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdateCompensationsPaidWorker();

        void loadQueryMoreCompensationsPaidWorker(int skip);
    }
}
