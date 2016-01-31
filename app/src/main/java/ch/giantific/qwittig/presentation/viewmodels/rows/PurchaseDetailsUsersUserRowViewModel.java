/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.Bindable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Created by fabio on 16.01.16.
 */
public class PurchaseDetailsUsersUserRowViewModel extends UserAvatarRowBaseViewModel {

    private boolean mUserNameBold;

    public PurchaseDetailsUsersUserRowViewModel(@NonNull User user, boolean userNameBold) {
        super(user);

        mUserNameBold = userNameBold;
    }

    @Bindable
    public boolean isUserNameBold() {
        return mUserNameBold;
    }

    public void setUserNameBold(boolean userNameBold) {
        mUserNameBold = userNameBold;
        notifyPropertyChanged(BR.userNameBold);
    }
}
