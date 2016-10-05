/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;

/**
 * Defines a view model for header that show a user's current identity balance.
 */
public interface FinanceHeaderContract {

    interface Presenter extends BasePresenter<ViewListener> {

        FinanceHeaderViewModel getViewModel();
    }

    interface ViewListener extends BaseViewListener {

        void setColorTheme(@NonNull BigFraction balance);

        void startEnterTransition();
    }
}
