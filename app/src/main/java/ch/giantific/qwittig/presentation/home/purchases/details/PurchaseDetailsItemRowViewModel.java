/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Created by fabio on 30.01.16.
 */
public interface PurchaseDetailsItemRowViewModel extends Observable {

    @Bindable
    String getItemName();

    @Bindable
    String getItemPrice();

    @Bindable
    float getItemAlpha();

    @Bindable
    float getItemUserPercentage();
}
