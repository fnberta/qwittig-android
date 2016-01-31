/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Created by fabio on 18.01.16.
 */
public abstract class UserAvatarRowBaseViewModel extends BaseObservable {

    User mUser;

    public UserAvatarRowBaseViewModel(@NonNull User user) {
        mUser = user;
    }

    public void setUser(@NonNull User user) {
        mUser = user;
        notifyPropertyChanged(BR.userName);
        notifyPropertyChanged(BR.userAvatar);
    }

    @Bindable
    public String getUserName() {
        // TODO: check if user is deleted and show me for current user
        return mUser.getNickname();
    }

    @Bindable
    public byte[] getUserAvatar() {
        return mUser.getAvatar();
    }
}
