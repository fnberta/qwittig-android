/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.presentation.common.viewmodels.DataRefreshViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Defines a view model for header that show a user's current identity balance.
 */
public interface BalanceHeaderViewModel extends ViewModel<BalanceHeaderViewModel.ViewListener>, DataRefreshViewModel {

    @Bindable
    String getCurrentIdentityBalance();

    interface ViewListener extends ViewModel.ViewListener {

        void setColorTheme(@NonNull BigFraction balance);
    }
}
