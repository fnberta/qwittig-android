/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.presenters.SortedListPresenter;
import ch.giantific.qwittig.presentation.common.views.SortedListView;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.items.CompUnpaidItemViewModel;

/**
 * Defines the view model for a screen showing a list of unpaid compensations.
 */
public interface CompsUnpaidContract {

    interface Presenter extends BasePresenter<ViewListener>,
            SortedListPresenter<CompUnpaidItemViewModel>,
            CompConfirmAmountDialogFragment.DialogInteractionListener {

        void onConfirmButtonClick(@NonNull CompUnpaidItemViewModel itemViewModel);

        void onRemindButtonClick(@NonNull CompUnpaidItemViewModel itemViewModel);
    }

    interface ViewListener extends BaseViewListener, SortedListView<CompUnpaidItemViewModel> {

        void showConfirmAmountDialog(@NonNull BigFraction amount,
                                     @NonNull String debtorNickname,
                                     @NonNull String currency);

        CompUnpaidItemViewModel getItemForId(@NonNull String id);
    }
}
