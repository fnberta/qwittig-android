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

    private final String name;
    private final String price;
    private final float alpha;
    private final float percentage;

    public PurchaseDetailsItemModel(@NonNull Item item,
                                    @NonNull String currentIdentityId,
                                    @NonNull NumberFormat numberFormat) {
        name = item.getName();
        price = numberFormat.format(item.getPrice());

        final Set<String> identities = item.getIdentitiesIds();
        if (identities.contains(currentIdentityId)) {
            alpha = 1f;
            percentage = (1f / identities.size()) * 100;
        } else {
            alpha = DISABLED_ALPHA;
            percentage = 0;
        }
    }

    @Bindable
    public String getName() {
        return name;
    }

    @Bindable
    public String getPrice() {
        return price;
    }

    @Bindable
    public float getAlpha() {
        return alpha;
    }

    @Bindable
    public float getPercentage() {
        return percentage;
    }
}
