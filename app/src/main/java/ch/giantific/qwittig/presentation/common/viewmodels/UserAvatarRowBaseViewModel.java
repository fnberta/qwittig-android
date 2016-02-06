/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Identity;

/**
 * Created by fabio on 18.01.16.
 */
public abstract class UserAvatarRowBaseViewModel extends BaseObservable {

    private String mIdentityName;
    private byte[] mIdentityAvatar;

    public UserAvatarRowBaseViewModel(@NonNull Identity identity) {
        setIdentity(identity);
    }

    @CallSuper
    protected void setIdentity(@NonNull Identity identity) {
        // TODO: show me for current user
        mIdentityName = identity.getNickname();
        mIdentityAvatar = identity.getAvatar();
    }

    @CallSuper
    protected void updateIdentity(@NonNull Identity identity) {
        setIdentity(identity);
    }

    @Bindable
    public String getIdentityName() {
        return mIdentityName;
    }

    @Bindable
    public byte[] getIdentityAvatar() {
        return mIdentityAvatar;
    }
}