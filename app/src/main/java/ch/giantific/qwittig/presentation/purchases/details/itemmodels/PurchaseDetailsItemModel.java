/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Set;

import ch.giantific.qwittig.domain.models.Item;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Provides an implementation of the {@link PurchaseDetailsItemModel} for a purchase item.
 */
public class PurchaseDetailsItemModel extends BaseObservable {

    private final String mName;
    private final String mPrice;
    private final float mAlpha;
    private final float mUserPercentage;

    public PurchaseDetailsItemModel(@NonNull Item item,
                                    @NonNull String currentIdentityId,
                                    @NonNull NumberFormat numberFormat) {
        mName = item.getName();
        mPrice = numberFormat.format(item.getPrice());

        final Set<String> identities = item.getIdentitiesIds();
        if (identities.contains(currentIdentityId)) {
            mAlpha = 1f;
            mUserPercentage = (1f / identities.size()) * 100;
        } else {
            mAlpha = DISABLED_ALPHA;
            mUserPercentage = 0;
        }
    }

    @Bindable
    public String getName() {
        return mName;
    }

    @Bindable
    public String getPrice() {
        return mPrice;
    }

    @Bindable
    public float getAlpha() {
        return mAlpha;
    }

    @Bindable
    public float getUserPercentage() {
        return mUserPercentage;
    }
}
