/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.IdentityAvatarRowBaseViewModel;

/**
 * Provides a row showing an identity with the avatar and the nickname (bold for the buyer of the
 * purchase).
 * <p/>
 * Subclass of {@link IdentityAvatarRowBaseViewModel}.
 */
public class PurchaseDetailsIdentitiesItemIdentity extends IdentityAvatarRowBaseViewModel {

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
