/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

/**
 * Created by fabio on 11.02.16.
 */
public class TotalItem extends BaseObservable implements DetailsItem {

    private final String mPurchaseTotal;
    private final String mPurchaseTotalForeign;

    public TotalItem(@NonNull String total, @NonNull String totalForeign) {
        mPurchaseTotal = total;
        mPurchaseTotalForeign = totalForeign;
    }

    @Bindable
    public String getPurchaseTotal() {
        return mPurchaseTotal;
    }

    @Bindable
    public String getPurchaseTotalForeign() {
        return mPurchaseTotalForeign;
    }

    @Override
    public int getType() {
        return Type.TOTAL;
    }
}
