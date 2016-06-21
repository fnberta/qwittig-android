/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.items;

import android.support.annotation.NonNull;

import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;

/**
 * Provides an implementation of the {@link PurchaseDetailsBaseItem} for a row showing the list of
 * identities.
 */
public class PurchaseDetailsIdentitiesItem implements PurchaseDetailsBaseItem {

    private final List<Identity> mIdentities;

    public PurchaseDetailsIdentitiesItem(@NonNull List<Identity> identities) {
        mIdentities = identities;
    }

    public List<Identity> getIdentities() {
        return mIdentities;
    }

    @Override
    public int getType() {
        return Type.IDENTITIES;
    }
}
