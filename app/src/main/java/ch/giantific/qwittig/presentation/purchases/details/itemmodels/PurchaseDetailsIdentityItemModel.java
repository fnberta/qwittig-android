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

    private final String mNickname;
    private final String mAvatar;
    private final boolean mNicknameBold;
    private final boolean mActive;

    public PurchaseDetailsIdentityItemModel(@NonNull Identity identity, boolean nicknameBold) {
        super();

        mNickname = identity.getNickname();
        mAvatar = identity.getAvatar();
        mNicknameBold = nicknameBold;
        mActive = identity.isActive();
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    @Bindable
    public boolean isNicknameBold() {
        return mNicknameBold;
    }

    public boolean isActive() {
        return mActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PurchaseDetailsIdentityItemModel itemModel = (PurchaseDetailsIdentityItemModel) o;

        if (mNicknameBold != itemModel.isNicknameBold()) return false;
        if (mActive != itemModel.isActive()) return false;
        if (!mNickname.equals(itemModel.getNickname())) return false;
        return mAvatar != null ? mAvatar.equals(itemModel.getAvatar()) : itemModel.getAvatar() == null;

    }

    @Override
    public int hashCode() {
        int result = mNickname.hashCode();
        result = 31 * result + (mAvatar != null ? mAvatar.hashCode() : 0);
        result = 31 * result + (mNicknameBold ? 1 : 0);
        result = 31 * result + (mActive ? 1 : 0);
        return result;
    }
}
