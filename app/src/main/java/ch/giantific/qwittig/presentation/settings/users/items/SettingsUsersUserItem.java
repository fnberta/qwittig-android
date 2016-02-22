/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import ch.giantific.qwittig.BR;

/**
 * Provides an implementation of the {@link SettingsUsersBaseItem} for a group user row.
 */
public class SettingsUsersUserItem extends BaseObservable implements SettingsUsersBaseItem, Comparable<SettingsUsersUserItem> {

    public static final Parcelable.Creator<SettingsUsersUserItem> CREATOR = new Parcelable.Creator<SettingsUsersUserItem>() {
        @Override
        public SettingsUsersUserItem createFromParcel(Parcel source) {
            return new SettingsUsersUserItem(source);
        }

        @Override
        public SettingsUsersUserItem[] newArray(int size) {
            return new SettingsUsersUserItem[size];
        }
    };
    private ShareListener mShareListener;
    private String mNickname;
    private String mShareLink;

    public SettingsUsersUserItem(@NonNull String nickname) {
        mNickname = nickname;
    }

    public SettingsUsersUserItem(@NonNull String nickname, @NonNull String shareLink) {
        mNickname = nickname;
        mShareLink = shareLink;
    }

    private SettingsUsersUserItem(Parcel in) {
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

    @Bindable
    public boolean isPending() {
        return !TextUtils.isEmpty(mShareLink);
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

    @Override
    public int compareTo(@NonNull SettingsUsersUserItem another) {
        return mNickname.compareToIgnoreCase(another.getNickname());
    }

    /**
     * Defines the actions to take when the user wants to share the invitation link.
     */
    public interface ShareListener {
        /**
         * Called when the share link button is clicked
         *
         * @param shareLink the invitation link for identity
         */
        void onShareClick(@NonNull String shareLink);
    }
}
