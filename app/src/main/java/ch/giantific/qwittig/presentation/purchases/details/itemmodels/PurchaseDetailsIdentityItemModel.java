/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;

/**
 * Provides a row showing an identity with the avatar and the nickname (bold for the buyer of the
 * purchase).
 */
public class PurchaseDetailsIdentityItemModel extends BaseObservable {

    private final String nickname;
    private final String avatar;
    private final boolean nicknameBold;
    private final boolean active;

    public PurchaseDetailsIdentityItemModel(@NonNull Identity identity, boolean nicknameBold) {
        super();

        nickname = identity.getNickname();
        avatar = identity.getAvatar();
        this.nicknameBold = nicknameBold;
        active = identity.isActive();
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    @Bindable
    public boolean isNicknameBold() {
        return nicknameBold;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PurchaseDetailsIdentityItemModel itemModel = (PurchaseDetailsIdentityItemModel) o;

        if (nicknameBold != itemModel.isNicknameBold()) return false;
        if (active != itemModel.isActive()) return false;
        if (!nickname.equals(itemModel.getNickname())) return false;
        return avatar != null ? avatar.equals(itemModel.getAvatar()) : itemModel.getAvatar() == null;

    }

    @Override
    public int hashCode() {
        int result = nickname.hashCode();
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (nicknameBold ? 1 : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }
}
