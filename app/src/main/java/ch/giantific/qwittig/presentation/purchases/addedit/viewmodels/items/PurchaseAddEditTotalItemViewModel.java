/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;

/**
 * Defines the view model for a total row in the add or edit purchase screen.
 */
public interface PurchaseAddEditTotalItemViewModel extends Observable {

    double getTotal();

    @Bindable
    String getTotalFormatted();

    void setTotal(double totalPrice, @NonNull String totalFormatted);

    @Bindable
    String getMyShare();

    void setMyShare(@NonNull String myShareFormatted);

    @Bindable
    String getCurrency();

    void setCurrency(@NonNull String currency);

    @Bindable
    int getCurrencySelected();

    void setCurrencySelected(int currencySelected);

    double getExchangeRate();

    @Bindable
    String getExchangeRateFormatted();

    void setExchangeRate(double exchangeRate, @NonNull String exchangeRateFormatted);

    @Bindable
    boolean isExchangeRateVisible();
}
