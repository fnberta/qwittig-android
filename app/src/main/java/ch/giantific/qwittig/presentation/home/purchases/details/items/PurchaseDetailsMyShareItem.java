/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

/**
 * Provides an implementation of the {@link PurchaseDetailsBaseItem} for the my share row.
 */
public class PurchaseDetailsMyShareItem extends BaseObservable implements PurchaseDetailsBaseItem {

    private final String mPurchaseMyShare;
    private final String mPurchaseMyShareForeign;

    public PurchaseDetailsMyShareItem(@NonNull String myShare, @NonNull String myShareForeign) {
        mPurchaseMyShare = myShare;
        mPurchaseMyShareForeign = myShareForeign;
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
