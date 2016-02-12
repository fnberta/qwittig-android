/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details.items;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.UserAvatarRowBaseViewModel;

/**
 * Created by fabio on 16.01.16.
 */
public class IdentitiesItemIdentity extends UserAvatarRowBaseViewModel {

    private boolean mNicknameBold;

    public IdentitiesItemIdentity(@NonNull Identity identity, boolean nicknameBold) {
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
