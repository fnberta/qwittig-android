/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by fabio on 24.01.16.
 */
public class RowItemUser implements Parcelable {

    public static final Creator<RowItemUser> CREATOR = new Creator<RowItemUser>() {
        public RowItemUser createFromParcel(Parcel source) {
            return new RowItemUser(source);
        }

        public RowItemUser[] newArray(int size) {
            return new RowItemUser[size];
        }
    };
    private String mObjectId;
    private String mNickname;
    private byte[] mAvatar;
    private boolean mSelected;

    public RowItemUser(@NonNull String objectId, @NonNull String nickname, @Nullable byte[] avatar) {
        this(objectId, nickname, avatar, true);
    }

    public RowItemUser(@NonNull String objectId, @NonNull String nickname, @Nullable byte[] avatar,
                       boolean selected) {
        mObjectId = objectId;
        mNickname = nickname;
        mAvatar = avatar;
        mSelected = selected;
    }

    protected RowItemUser(Parcel in) {
        mObjectId = in.readString();
        mNickname = in.readString();
        mAvatar = in.createByteArray();
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
    public byte[] getAvatar() {
        return mAvatar;
    }

    public void setAvatar(@Nullable byte[] avatar) {
        mAvatar = avatar;
    }

    @Bindable
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
        dest.writeString(mObjectId);
        dest.writeString(mNickname);
        dest.writeByteArray(mAvatar);
        dest.writeByte(mSelected ? (byte) 1 : (byte) 0);
    }
}
