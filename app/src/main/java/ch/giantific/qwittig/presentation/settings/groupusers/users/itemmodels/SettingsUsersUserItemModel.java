/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseChildItemModel;
import timber.log.Timber;

/**
 * Defines an observable view model for an invited user row.
 */
public class SettingsUsersUserItemModel extends BaseChildItemModel
        implements Comparable<SettingsUsersUserItemModel> {

    private final String mNickname;
    private final String mAvatar;
    private final BigFraction mBalance;
    private final String mInvitationLink;
    private final String mGroupId;

    public SettingsUsersUserItemModel(@EventType int eventType,
                                      @NonNull Identity identity) {
        super(eventType, identity.getId());

        mNickname = identity.getNickname();
        mAvatar = identity.getAvatar();
        mBalance = identity.getBalanceFraction();
        mInvitationLink = identity.getInvitationLink();
        mGroupId = identity.getGroup();
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    public BigFraction getBalance() {
        return mBalance;
    }

    public String getInvitationLink() {
        return mInvitationLink;
    }

    public String getGroupId() {
        return mGroupId;
    }

    @Bindable
    public boolean isPending() {
        return !TextUtils.isEmpty(mInvitationLink);
    }

    @Override
    public int compareTo(@NonNull SettingsUsersUserItemModel another) {
        if (isPending() && !another.isPending() || another.isPending() && !isPending()) {
            return -1;
        }

        return mNickname.compareToIgnoreCase(another.getNickname());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SettingsUsersUserItemModel that = (SettingsUsersUserItemModel) o;

        if (!mNickname.equals(that.getNickname())) return false;
        if (mAvatar != null ? !mAvatar.equals(that.getAvatar()) : that.getAvatar() != null)
            return false;
        if (!mBalance.equals(that.getBalance())) return false;
        return mInvitationLink != null ? mInvitationLink.equals(that.getInvitationLink()) : that.getInvitationLink() == null;
    }

    @Override
    public int hashCode() {
        int result = mNickname.hashCode();
        result = 31 * result + (mAvatar != null ? mAvatar.hashCode() : 0);
        result = 31 * result + mBalance.hashCode();
        result = 31 * result + (mInvitationLink != null ? mInvitationLink.hashCode() : 0);
        return result;
    }
}
