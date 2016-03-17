/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;
import ch.giantific.qwittig.presentation.finance.BalanceHeaderViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.items.CompsUnpaidBaseItem;

/**
 * Defines the view model for a screen showing a list of unpaid compensations.
 */
public interface CompsUnpaidViewModel extends OnlineListViewModel<CompsUnpaidBaseItem>,
        CompRemindWorkerListener,
        CompsUnpaidRecyclerAdapter.AdapterInteractionListener,
        CompConfirmAmountDialogFragment.DialogInteractionListener {

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends OnlineListViewModel.ViewListener {

        void startUpdateCompensationsUnpaidService();

        void loadCompensationRemindWorker(@NonNull String compensationId);

        void showCompensationAmountConfirmDialog(@NonNull BigFraction amount,
                                                 @NonNull String debtorNickname,
                                                 @NonNull String currency);

        void onCompensationConfirmed();
    }
}
