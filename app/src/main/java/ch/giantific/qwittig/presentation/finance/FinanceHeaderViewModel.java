/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.di.scopes.PerActivity;

/**
 * Defines an observable view model for the header showing the user's balance.
 */
@PerActivity
public class FinanceHeaderViewModel extends BaseObservable {

    private String balance;

    @Inject
    public FinanceHeaderViewModel() {
    }

    @Bindable
    public String getBalance() {
        return balance;
    }

    public void setBalance(@NonNull String balance) {
        this.balance = balance;
        notifyPropertyChanged(BR.balance);
    }
}
