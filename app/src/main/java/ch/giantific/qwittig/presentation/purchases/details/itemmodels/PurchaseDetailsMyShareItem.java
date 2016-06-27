/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;

/**
 * Provides an implementation of the {@link PurchaseDetailsItemModel} for the my share row.
 */
public class PurchaseDetailsMyShareItem extends BaseObservable implements PurchaseDetailsItemModel {

    private final String mPurchaseMyShare;
    private final String mPurchaseMyShareForeign;

    public PurchaseDetailsMyShareItem(double myShare,
                                      double myShareForeign,
                                      @NonNull NumberFormat moneyFormatter,
                                      @NonNull NumberFormat foreignFormatter) {
        mPurchaseMyShare = moneyFormatter.format(myShare);
        mPurchaseMyShareForeign = foreignFormatter.format(myShareForeign);
    }

    @Bindable
    public String getPurchaseMyShare() {
        return mPurchaseMyShare;
    }

    @Bindable
    public String getPurchaseMyShareForeign() {
        return mPurchaseMyShareForeign;
    }

    @Override
    public int getType() {
        return Type.MY_SHARE;
    }
}
