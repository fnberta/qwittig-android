/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsQueryMoreListener;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsUpdateListener;

/**
 * Created by fabio on 18.01.16.
 */
public interface FinanceCompsPaidViewModel extends OnlineListViewModel<Compensation>,
        CompensationsUpdateListener, CompensationsQueryMoreListener, LoadMoreViewModel {


    int TYPE_ITEM = 0;
    int TYPE_PROGRESS = 1;

    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdateCompensationsPaidWorker();

        void loadQueryMoreCompensationsPaidWorker(int skip);
    }
}
