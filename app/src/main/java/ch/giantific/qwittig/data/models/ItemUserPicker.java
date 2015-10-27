/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Represents a user with a name and avatar image.
 * <p/>
 * Can be passed around android components by implementing {@link Parcelable}.
 */
public class ItemUserPicker implements Parcelable, Comparable<ItemUserPicker> {

    public static final Parcelable.Creator<ItemUserPicker> CREATOR = new Parcelable.Creator<ItemUserPicker>() {
        @NonNull
        public ItemUserPicker createFromParcel(@NonNull Parcel source) {
            return new ItemUserPicker(source);
        }

        @NonNull
        public ItemUserPicker[] newArray(int size) {
            return new ItemUserPicker[size];
        }
    };
    private String mObjectId;
    private String mNickname;
    private byte[] mAvatar;

    public ItemUserPicker(@NonNull String objectId, @NonNull String nickname,
                          @NonNull byte[] avatar) {
        mObjectId = objectId;
        mNickname = nickname;
        mAvatar = avatar;
    }

    private ItemUserPicker(@NonNull Parcel in) {
        this.mObjectId = in.readString();
        this.mNickname = in.readString();
        this.mAvatar = in.createByteArray();
    }

    public String getObjectId() {
        return mObjectId;
    }

    public void setObjectId(@NonNull String objectId) {
        mObjectId = objectId;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(@NonNull String nickname) {
        mNickname = nickname;
    }

    public byte[] getAvatar() {
        return mAvatar;
    }

    public void setAvatar(@NonNull byte[] avatar) {
        mAvatar = avatar;
    }

    @Override
    public int compareTo(@NonNull ItemUserPicker another) {
        return this.getNickname().compareToIgnoreCase(another.getNickname());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.mObjectId);
        dest.writeString(this.mNickname);
        dest.writeByteArray(this.mAvatar);
    }
}
