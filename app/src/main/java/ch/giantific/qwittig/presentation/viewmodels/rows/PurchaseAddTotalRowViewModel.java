/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import java.math.BigDecimal;

/**
 * Created by fabio on 26.01.16.
 */
public interface PurchaseAddTotalRowViewModel extends Observable {

    @Bindable
    String getTotalPrice();

    void setTotalPrice(@NonNull BigDecimal totalPrice);

    @Bindable
    String getMyShare();

    void setMyShare(@NonNull BigDecimal myShare);

    @Bindable
    String getCurrency();

    void onCurrencySelected(@NonNull AdapterView<?> parent, View view, int position, long id);
}
