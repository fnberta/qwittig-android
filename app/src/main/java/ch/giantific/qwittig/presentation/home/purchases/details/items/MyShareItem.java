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
public class MyShareItem extends BaseObservable implements DetailsItem {

    private String mPurchaseMyShare;
    private String mPurchaseMyShareForeign;

    public MyShareItem(@NonNull String myShare, @NonNull String myShareForegin) {
        mPurchaseMyShare = myShare;
        mPurchaseMyShareForeign = myShareForegin;
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
