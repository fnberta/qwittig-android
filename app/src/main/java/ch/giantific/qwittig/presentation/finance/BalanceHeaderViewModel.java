/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Created by fabio on 03.02.16.
 */
public interface BalanceHeaderViewModel extends Observable {

    @Bindable
    String getCurrentIdentityBalance();
}
