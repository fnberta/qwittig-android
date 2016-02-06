/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.details;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.UserAvatarRowBaseViewModel;

/**
 * Created by fabio on 16.01.16.
 */
public class PurchaseDetailsUsersUserRowViewModel extends UserAvatarRowBaseViewModel {

    private boolean mNicknameBold;

    public PurchaseDetailsUsersUserRowViewModel(@NonNull Identity identity, boolean nicknameBold) {
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
