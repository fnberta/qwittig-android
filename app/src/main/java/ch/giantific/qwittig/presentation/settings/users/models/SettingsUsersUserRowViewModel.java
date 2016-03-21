/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import ch.giantific.qwittig.domain.models.Identity;

/**
 * Defines an observable view model for an invited user row.
 */
public class SettingsUsersUserRowViewModel extends BaseObservable
        implements Comparable<SettingsUsersUserRowViewModel> {

    private final ShareListener mShareListener;
    private final Identity mIdentity;
    private String mShareLink;

    public SettingsUsersUserRowViewModel(@NonNull ShareListener shareListener, @NonNull Identity identity) {
        mShareListener = shareListener;
        mIdentity = identity;
    }

    public SettingsUsersUserRowViewModel(@NonNull ShareListener shareListener, @NonNull Identity identity,
                                         @NonNull String shareLink) {
        mShareListener = shareListener;
        mIdentity = identity;
        mShareLink = shareLink;
    }

    public Identity getIdentity() {
        return mIdentity;
    }

    @Bindable
    public String getNickname() {
        return mIdentity.getNickname();
    }

    @Bindable
    public boolean isPending() {
        return !TextUtils.isEmpty(mShareLink);
    }

    public void onShareClick(View view) {
        mShareListener.onShareClick(mShareLink);
    }

    @Override
    public int compareTo(@NonNull SettingsUsersUserRowViewModel another) {
        return getNickname().compareToIgnoreCase(another.getNickname());
    }

    /**
     * Defines the actions to take when the user wants to share the invitation link.
     */
    public interface ShareListener {
        /**
         * Called when the share link button is clicked
         *
         * @param shareLink the invitation link for identity
         */
        void onShareClick(@NonNull String shareLink);
    }
}
