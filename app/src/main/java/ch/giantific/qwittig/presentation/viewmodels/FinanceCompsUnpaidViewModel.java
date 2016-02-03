/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.domain.models.CompensationUnpaidItem;
import ch.giantific.qwittig.presentation.ui.adapters.CompensationsUnpaidRecyclerAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.CompensationChangeAmountDialogFragment;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsUpdateListener;
import ch.giantific.qwittig.presentation.workerfragments.reminder.CompensationReminderListener;

/**
 * Created by fabio on 18.01.16.
 */
public interface FinanceCompsUnpaidViewModel extends OnlineListViewModel<CompensationUnpaidItem, FinanceCompsUnpaidViewModel.ViewListener>,
        FinanceHeaderViewModel, CompensationsUpdateListener, CompensationReminderListener,
        CompensationsUnpaidRecyclerAdapter.AdapterInteractionListener,
        CompensationChangeAmountDialogFragment.DialogInteractionListener {

    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdateCompensationsUnpaidWorker();

        void loadCompensationRemindWorker(@NonNull String compensationId);

        void showCompensationAmountConfirmDialog(@NonNull BigFraction amount,
                                                 @NonNull String currency);

        void setColorTheme(@NonNull BigFraction balance);
    }
}
