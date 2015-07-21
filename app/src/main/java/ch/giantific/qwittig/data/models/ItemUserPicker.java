package ch.giantific.qwittig.data.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by fabio on 20.03.15.
 */
public class ItemUserPicker implements Parcelable, Comparable<ItemUserPicker> {

    private String mObjectId;
    private String mNickname;
    private byte[] mAvatar;

    public String getObjectId() {
        return mObjectId;
    }

    public void setObjectId(String objectId) {
        mObjectId = objectId;
    }

    public String getNickname() {
        return mNickname;
    }

    public void setNickname(String nickname) {
        mNickname = nickname;
    }

    public byte[] getAvatar() {
        return mAvatar;
    }

    public void setAvatar(byte[] avatar) {
        mAvatar = avatar;
    }

    public ItemUserPicker(String objectId, String nickname, byte[] avatar) {
        mObjectId = objectId;
        mNickname = nickname;
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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mObjectId);
        dest.writeString(this.mNickname);
        dest.writeByteArray(this.mAvatar);
    }

    private ItemUserPicker(Parcel in) {
        this.mObjectId = in.readString();
        this.mNickname = in.readString();
        this.mAvatar = in.createByteArray();
    }

    public static final Parcelable.Creator<ItemUserPicker> CREATOR = new Parcelable.Creator<ItemUserPicker>() {
        public ItemUserPicker createFromParcel(Parcel source) {
            return new ItemUserPicker(source);
        }

        public ItemUserPicker[] newArray(int size) {
            return new ItemUserPicker[size];
        }
    };
}
