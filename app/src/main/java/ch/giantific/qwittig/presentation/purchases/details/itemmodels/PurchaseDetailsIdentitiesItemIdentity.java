/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseIdentityAvatarItemModel;

/**
 * Provides a row showing an identity with the avatar and the nickname (bold for the buyer of the
 * purchase).
 * <p/>
 * Subclass of {@link BaseIdentityAvatarItemModel}.
 */
public class PurchaseDetailsIdentitiesItemIdentity extends BaseIdentityAvatarItemModel {

    private boolean mNicknameBold;

    public PurchaseDetailsIdentitiesItemIdentity(@NonNull Identity identity, boolean nicknameBold) {
        super(identity);

        mNicknameBold = nicknameBold;
    }

    public void updateIdentity(@NonNull Identity identity, boolean nicknameBold) {
        super.updateIdentity(identity);

        mNicknameBold = nicknameBold;
        notifyChange();
    }

    @Bindable
    public boolean isNicknameBold() {
        return mNicknameBold;
    }
}
