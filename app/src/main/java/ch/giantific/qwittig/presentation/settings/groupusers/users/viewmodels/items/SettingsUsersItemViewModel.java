/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.items;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.items.BaseChildItemViewModel;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;

/**
 * Defines an observable view model for an invited user row.
 */
public class SettingsUsersItemViewModel extends BaseChildItemViewModel
        implements Comparable<SettingsUsersItemViewModel> {

    private final boolean pending;
    private final String nickname;
    private final String avatar;
    private final BigFraction balance;
    private final String groupId;
    private final String groupName;

    public SettingsUsersItemViewModel(@EventType int eventType,
                                      @NonNull Identity identity,
                                      @NonNull String groupName) {
        super(eventType, identity.getId());

        pending = identity.isPending();
        nickname = identity.getNickname();
        avatar = identity.getAvatar();
        balance = identity.getBalanceFraction();
        groupId = identity.getGroup();
        this.groupName = groupName;
    }

    @Bindable
    public boolean isPending() {
        return !pending;
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

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public int compareTo(@NonNull SettingsUsersItemViewModel another) {
        if (isPending() && !another.isPending() || another.isPending() && !isPending()) {
            return -1;
        }

        return nickname.compareToIgnoreCase(another.getNickname());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SettingsUsersItemViewModel that = (SettingsUsersItemViewModel) o;

        if (pending != that.pending) return false;
        if (!nickname.equals(that.nickname)) return false;
        if (avatar != null ? !avatar.equals(that.avatar) : that.avatar != null) return false;
        if (!balance.equals(that.balance)) return false;
        if (!groupId.equals(that.groupId)) return false;
        return groupName.equals(that.groupName);
    }

    @Override
    public int hashCode() {
        int result = (pending ? 1 : 0);
        result = 31 * result + nickname.hashCode();
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + balance.hashCode();
        result = 31 * result + groupId.hashCode();
        result = 31 * result + groupName.hashCode();
        return result;
    }
}
