/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;

/**
 * Defines a view model for header that show a user's current identity balance.
 */
public interface FinanceHeaderContract {

    interface Presenter extends BasePresenter<ViewListener> {
        // empty
    }

    interface ViewListener extends BaseView {

        void setColorTheme(@NonNull BigFraction balance);

        void startEnterTransition();
    }
}
