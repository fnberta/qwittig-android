/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

import ch.giantific.qwittig.BR;

/**
 * Created by fabio on 07.02.16.
 */
public class UserItem extends BaseObservable implements AddUsersItem {

    public static final Parcelable.Creator<UserItem> CREATOR = new Parcelable.Creator<UserItem>() {
        @Override
        public UserItem createFromParcel(Parcel source) {
            return new UserItem(source);
        }

        @Override
        public UserItem[] newArray(int size) {
            return new UserItem[size];
        }
    };
    private ShareListener mShareListener;
    private String mNickname;
    private String mShareLink;

    public UserItem(@NonNull String nickname, @NonNull String shareLink) {
        mNickname = nickname;
        mShareLink = shareLink;
    }

    protected UserItem(Parcel in) {
        mNickname = in.readString();
        mShareLink = in.readString();
    }

    public void setShareListener(@NonNull ShareListener shareListener) {
        mShareListener = shareListener;
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    public void setNickname(@NonNull String nickname) {
        mNickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }

    public String getShareLink() {
        return mShareLink;
    }

    public void setShareLink(@NonNull String shareLink) {
        mShareLink = shareLink;
    }

    public void onShareClick(View view) {
        mShareListener.onShareClick(mShareLink);
    }

    @Override
    public int getType() {
        return Type.USER;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mNickname);
        dest.writeString(mShareLink);
    }

    public interface ShareListener {
        void onShareClick(@NonNull String shareLink);
    }
}
