/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.BR;

import static ch.giantific.qwittig.utils.ViewUtils.DISABLED_ALPHA;

/**
 * Represents a user with an object id, a nickname, an avatar and whether it is selected or not.
 */
public class PurchaseAddEditItemUsersUser extends BaseObservable implements Parcelable {

    public static final Creator<PurchaseAddEditItemUsersUser> CREATOR = new Creator<PurchaseAddEditItemUsersUser>() {
        @Override
        public PurchaseAddEditItemUsersUser createFromParcel(Parcel source) {
            return new PurchaseAddEditItemUsersUser(source);
        }

        @Override
        public PurchaseAddEditItemUsersUser[] newArray(int size) {
            return new PurchaseAddEditItemUsersUser[size];
        }
    };
    private String mObjectId;
    private String mNickname;
    private String mAvatar;
    private boolean mSelected;

    public PurchaseAddEditItemUsersUser(@NonNull String objectId, @NonNull String nickname, @Nullable String avatar,
                                        boolean selected) {
        mObjectId = objectId;
        mNickname = nickname;
        mAvatar = avatar;
        mSelected = selected;
    }

    private PurchaseAddEditItemUsersUser(Parcel in) {
        mObjectId = in.readString();
        mNickname = in.readString();
        mAvatar = in.readString();
        mSelected = in.readByte() != 0;
    }

    public String getObjectId() {
        return mObjectId;
    }

    public void setObjectId(String objectId) {
        mObjectId = objectId;
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    public void setNickname(@NonNull String nickname) {
        mNickname = nickname;
    }

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(@Nullable String avatar) {
        mAvatar = avatar;
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

    public void toggleSelected() {
        mSelected = !mSelected;
        notifyPropertyChanged(BR.alpha);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mObjectId);
        dest.writeString(mNickname);
        dest.writeString(mAvatar);
        dest.writeByte(mSelected ? (byte) 1 : (byte) 0);
    }
}
