/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseChildItemModel;

/**
 * Defines an observable view model for an invited user row.
 */
public class SettingsUsersUserItemModel extends BaseChildItemModel
        implements Comparable<SettingsUsersUserItemModel> {

    private final String nickname;
    private final String avatar;
    private final BigFraction balance;
    private final String invitationLink;
    private final String groupId;

    public SettingsUsersUserItemModel(@EventType int eventType,
                                      @NonNull Identity identity) {
        super(eventType, identity.getId());

        nickname = identity.getNickname();
        avatar = identity.getAvatar();
        balance = identity.getBalanceFraction();
        invitationLink = identity.getInvitationLink();
        groupId = identity.getGroup();
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public BigFraction getBalance() {
        return balance;
    }

    public String getInvitationLink() {
        return invitationLink;
    }

    public String getGroupId() {
        return groupId;
    }

    @Bindable
    public boolean isPending() {
        return !TextUtils.isEmpty(invitationLink);
    }

    @Override
    public int compareTo(@NonNull SettingsUsersUserItemModel another) {
        if (isPending() && !another.isPending() || another.isPending() && !isPending()) {
            return -1;
        }

        return nickname.compareToIgnoreCase(another.getNickname());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SettingsUsersUserItemModel that = (SettingsUsersUserItemModel) o;

        if (!nickname.equals(that.getNickname())) return false;
        if (avatar != null ? !avatar.equals(that.getAvatar()) : that.getAvatar() != null)
            return false;
        if (!balance.equals(that.getBalance())) return false;
        return invitationLink != null ? invitationLink.equals(that.getInvitationLink()) : that.getInvitationLink() == null;
    }

    @Override
    public int hashCode() {
        int result = nickname.hashCode();
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + balance.hashCode();
        result = 31 * result + (invitationLink != null ? invitationLink.hashCode() : 0);
        return result;
    }
}
