/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Represents a user with an object id, a nickname, an avatar and whether it is selected or not.
 */
public class PurchaseAddEditItemIdentity extends BaseObservable implements Parcelable {

    public static final Creator<PurchaseAddEditItemIdentity> CREATOR = new Creator<PurchaseAddEditItemIdentity>() {
        @Override
        public PurchaseAddEditItemIdentity createFromParcel(Parcel source) {
            return new PurchaseAddEditItemIdentity(source);
        }

        @Override
        public PurchaseAddEditItemIdentity[] newArray(int size) {
            return new PurchaseAddEditItemIdentity[size];
        }
    };
    private final String mIdentityId;
    private final String mNickname;
    private final String mAvatar;
    private boolean mSelected;

    public PurchaseAddEditItemIdentity(@NonNull String identityId, @NonNull String nickname,
                                       @Nullable String avatar, boolean selected) {
        mIdentityId = identityId;
        mNickname = nickname;
        mAvatar = avatar;
        mSelected = selected;
    }

    private PurchaseAddEditItemIdentity(Parcel in) {
        mIdentityId = in.readString();
        mNickname = in.readString();
        mAvatar = in.readString();
        mSelected = in.readByte() != 0;
    }

    public String getIdentityId() {
        return mIdentityId;
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
    public float getAlpha() {
        return mSelected ? 1f : DISABLED_ALPHA;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mIdentityId);
        dest.writeString(mNickname);
        dest.writeString(mAvatar);
        dest.writeByte(mSelected ? (byte) 1 : (byte) 0);
    }
}
