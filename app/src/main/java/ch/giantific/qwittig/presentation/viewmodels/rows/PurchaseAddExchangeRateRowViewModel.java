/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.view.View;

/**
 * Created by fabio on 26.01.16.
 */
public interface PurchaseAddExchangeRateRowViewModel extends Observable {

    @Bindable
    String getExchangeRate();

    void setExchangeRate(float exchangeRate);

    @Bindable
    boolean isExchangeRateVisible();

    void onExchangeRateClick(View view);
}
