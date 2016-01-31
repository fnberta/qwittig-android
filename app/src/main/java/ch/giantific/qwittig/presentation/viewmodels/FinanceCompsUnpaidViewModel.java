/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.presentation.ui.adapters.CompensationsUnpaidRecyclerAdapter;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.CompensationChangeAmountDialogFragment;
import ch.giantific.qwittig.presentation.workerfragments.query.CompensationsUpdateListener;
import ch.giantific.qwittig.presentation.workerfragments.reminder.CompensationReminderListener;

/**
 * Created by fabio on 18.01.16.
 */
public interface FinanceCompsUnpaidViewModel extends OnlineListViewModel<Compensation, FinanceCompsUnpaidViewModel.ViewListener>,
        CompensationsUpdateListener, CompensationReminderListener,
        CompensationsUnpaidRecyclerAdapter.AdapterInteractionListener,
        CompensationChangeAmountDialogFragment.DialogInteractionListener {

    int TYPE_PENDING_POS = 1;
    int TYPE_PENDING_NEG = 2;

    interface ViewListener extends OnlineListViewModel.ViewListener {

        void loadUpdateCompensationsUnpaidWorker();

        void loadCompensationRemindWorker(@NonNull String compensationId);

        void showChangeCompensationAmountDialog(@NonNull BigFraction amount, @NonNull String currency);
    }
}
