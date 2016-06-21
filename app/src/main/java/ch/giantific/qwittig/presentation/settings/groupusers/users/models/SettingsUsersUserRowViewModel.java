/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;

/**
 * Defines an observable view model for an invited user row.
 */
public class SettingsUsersUserRowViewModel extends BaseObservable
        implements Comparable<SettingsUsersUserRowViewModel> {

    private final Identity mIdentity;

    public SettingsUsersUserRowViewModel(@NonNull Identity identity) {
        mIdentity = identity;
    }

    public Identity getIdentity() {
        return mIdentity;
    }

    @Bindable
    public String getNickname() {
        return mIdentity.getNickname();
    }

    @Bindable
    public String getAvatar() {
        return mIdentity.getAvatarUrl();
    }

    @Bindable
    public boolean isPending() {
        return mIdentity.isPending();
    }

    @Override
    public int compareTo(@NonNull SettingsUsersUserRowViewModel another) {
        return getNickname().compareToIgnoreCase(another.getNickname());
    }
}
