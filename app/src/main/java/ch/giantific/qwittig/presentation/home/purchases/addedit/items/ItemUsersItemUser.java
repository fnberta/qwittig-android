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
 * Created by fabio on 24.01.16.
 */
public class ItemUsersItemUser extends BaseObservable implements Parcelable {

    public static final Creator<ItemUsersItemUser> CREATOR = new Creator<ItemUsersItemUser>() {
        @Override
        public ItemUsersItemUser createFromParcel(Parcel source) {
            return new ItemUsersItemUser(source);
        }

        @Override
        public ItemUsersItemUser[] newArray(int size) {
            return new ItemUsersItemUser[size];
        }
    };
    private String mObjectId;
    private String mNickname;
    private String mAvatar;
    private boolean mSelected;

    public ItemUsersItemUser(@NonNull String objectId, @NonNull String nickname, @Nullable String avatar,
                             boolean selected) {
        mObjectId = objectId;
        mNickname = nickname;
        mAvatar = avatar;
        mSelected = selected;
    }

    private ItemUsersItemUser(Parcel in) {
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
