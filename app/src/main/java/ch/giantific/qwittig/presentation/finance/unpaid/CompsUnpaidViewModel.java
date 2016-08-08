/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidItemModel;

/**
 * Defines the view model for a screen showing a list of unpaid compensations.
 */
public interface CompsUnpaidViewModel extends ListViewModel<CompsUnpaidItemModel, CompsUnpaidViewModel.ViewListener>,
        CompConfirmAmountDialogFragment.DialogInteractionListener {

    void onConfirmButtonClick(@NonNull CompsUnpaidItemModel itemModel);

    void onRemindButtonClick(@NonNull CompsUnpaidItemModel itemModel);

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener extends ViewModel.ViewListener {

        void showCompensationAmountConfirmDialog(@NonNull BigFraction amount,
                                                 @NonNull String debtorNickname,
                                                 @NonNull String currency);
    }
}
