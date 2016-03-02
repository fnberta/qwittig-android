/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

/**
 * Provides an implementation of the {@link PurchaseDetailsBaseItem} for the total row.
 */
public class PurchaseDetailsTotalItem extends BaseObservable implements PurchaseDetailsBaseItem {

    private final String mPurchaseTotal;
    private final String mPurchaseTotalForeign;

    public PurchaseDetailsTotalItem(@NonNull String total, @NonNull String totalForeign) {
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
