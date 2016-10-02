/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.presentation.common.presenters.ListPresenter;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.CompsUnpaidViewModel;
import ch.giantific.qwittig.presentation.finance.unpaid.viewmodels.items.CompUnpaidItemViewModel;

/**
 * Defines the view model for a screen showing a list of unpaid compensations.
 */
public interface CompsUnpaidContract {

    interface Presenter extends BasePresenter<ViewListener>,
            ListPresenter<CompUnpaidItemViewModel>,
            CompConfirmAmountDialogFragment.DialogInteractionListener {

        CompsUnpaidViewModel getViewModel();

        void setListInteraction(@NonNull ListInteraction listInteraction);

        void onConfirmButtonClick(@NonNull CompUnpaidItemViewModel itemViewModel);

        void onRemindButtonClick(@NonNull CompUnpaidItemViewModel itemViewModel);
    }

    interface ViewListener extends BaseViewListener {

        void showCompensationAmountConfirmDialog(@NonNull BigFraction amount,
                                                 @NonNull String debtorNickname,
                                                 @NonNull String currency);
    }
}
