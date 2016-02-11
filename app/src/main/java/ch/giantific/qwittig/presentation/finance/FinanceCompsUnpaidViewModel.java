/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModel;

/**
 * Created by fabio on 18.01.16.
 */
public interface FinanceCompsUnpaidViewModel extends OnlineListViewModel<CompensationUnpaidItem>,
        BalanceHeaderViewModel, CompensationsUpdateWorkerListener, CompensationReminderWorkerListener,
        CompensationsUnpaidRecyclerAdapter.AdapterInteractionListener,
        CompensationConfirmAmountDialogFragment.DialogInteractionListener {

    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdateCompensationsUnpaidWorker();

        void loadCompensationRemindWorker(@NonNull String compensationId);

        void showCompensationAmountConfirmDialog(@NonNull BigFraction amount,
                                                 @NonNull String debtorNickname,
                                                 @NonNull String currency);

        void setColorTheme(@NonNull BigFraction balance);
    }
}
