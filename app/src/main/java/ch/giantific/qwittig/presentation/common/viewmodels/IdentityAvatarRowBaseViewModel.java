/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;

/**
 * Provides an abstract base class for a list row representing an identity with the avatar and the
 * nickname.
 */
public abstract class IdentityAvatarRowBaseViewModel extends BaseObservable {

    private String mIdentityName;
    private String mIdentityAvatar;

    public IdentityAvatarRowBaseViewModel(@NonNull Identity identity) {
        setIdentity(identity);
    }

    @CallSuper
    protected void setIdentity(@NonNull Identity identity) {
        // TODO: show me for current user
        mIdentityName = identity.getNickname();
        mIdentityAvatar = identity.getAvatarUrl();
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
    public String getIdentityAvatar() {
        return mIdentityAvatar;
    }
}