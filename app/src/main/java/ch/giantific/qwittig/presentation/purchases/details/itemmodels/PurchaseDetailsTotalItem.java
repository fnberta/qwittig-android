/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;

/**
 * Provides an implementation of the {@link PurchaseDetailsItemModel} for the total row.
 */
public class PurchaseDetailsTotalItem extends BaseObservable implements PurchaseDetailsItemModel {

    private final String mPurchaseTotal;
    private final String mPurchaseTotalForeign;

    public PurchaseDetailsTotalItem(double totalPrice, double totalPriceForeign,
                                    @NonNull NumberFormat moneyFormatter,
                                    @NonNull NumberFormat foreignFormatter) {
        mPurchaseTotal = moneyFormatter.format(totalPrice);
        mPurchaseTotalForeign = foreignFormatter.format(totalPriceForeign);
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
