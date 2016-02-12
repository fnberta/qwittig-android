/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.support.annotation.NonNull;

import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;

/**
 * Created by fabio on 11.02.16.
 */
public class IdentitiesItem implements DetailsItem {

    private List<Identity> mIdentities;

    public IdentitiesItem(@NonNull List<Identity> identities) {
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
