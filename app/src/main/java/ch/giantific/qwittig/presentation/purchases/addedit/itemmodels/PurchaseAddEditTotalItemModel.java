/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

/**
 * Defines the view model for a total row in the add or edit purchase screen.
 */
public interface PurchaseAddEditTotalItemModel extends Observable {

    @Bindable
    String getTotal();

    void setTotal(double totalPrice);

    @Bindable
    String getMyShare();

    void setMyShare(double myShare);

    @Bindable
    String getCurrency();

    void setCurrency(@NonNull String currency);

    @Bindable
    int getCurrencySelected();

    void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);

    @Bindable
    String getExchangeRate();

    void setExchangeRate(double exchangeRate);

    @Bindable
    boolean isExchangeRateVisible();

    void onExchangeRateClick(View view);
}
