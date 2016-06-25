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
public abstract class BaseIdentityAvatarRowViewModel extends BaseObservable {

    private Identity mIdentity;

    public BaseIdentityAvatarRowViewModel(@NonNull Identity identity) {
        setIdentity(identity);
    }

    @CallSuper
    protected void setIdentity(@NonNull Identity identity) {
        // TODO: show me for current user
        mIdentity = identity;
    }

    @CallSuper
    protected void updateIdentity(@NonNull Identity identity) {
        setIdentity(identity);
    }

    @Bindable
    public String getIdentityName() {
        return mIdentity.getNickname();
    }

    @Bindable
    public String getIdentityAvatar() {
        return mIdentity.getAvatarUrl();
    }
}