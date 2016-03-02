/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Defines a view model for header that show a user's current identity balance.
 */
public interface BalanceHeaderViewModel extends Observable {

    @Bindable
    String getCurrentIdentityBalance();
}
