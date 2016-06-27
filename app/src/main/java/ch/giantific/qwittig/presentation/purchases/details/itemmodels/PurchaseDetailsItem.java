/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;
import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Provides an implementation of the {@link PurchaseDetailsItemModel} for a purchase item.
 */
public class PurchaseDetailsItem extends BaseObservable implements PurchaseDetailsItemModel {

    private final Item mItem;
    private final String mItemName;
    private final String mItemPrice;
    private final float mItemAlpha;
    private final float mItemUserPercentage;

    public PurchaseDetailsItem(@NonNull Item item, @NonNull Identity currentIdentity,
                               @NonNull NumberFormat formatter) {
        mItem = item;
        mItemName = item.getName();
        mItemPrice = formatter.format(item.getPrice());

        final List<Identity> identities = item.getIdentities();
        if (identities.contains(currentIdentity)) {
            mItemUserPercentage = (1f / identities.size()) * 100;
            mItemAlpha = 1f;
        } else {
            mItemUserPercentage = 0;
            mItemAlpha = DISABLED_ALPHA;
        }
    }

    public Item getItem() {
        return mItem;
    }

    @Bindable
    public String getItemName() {
        return mItemName;
    }

    @Bindable
    public String getItemPrice() {
        return mItemPrice;
    }

    @Bindable
    public float getItemAlpha() {
        return mItemAlpha;
    }

    @Bindable
    public float getItemUserPercentage() {
        return mItemUserPercentage;
    }

    @Override
    public int getType() {
        return Type.ITEM;
    }
}
