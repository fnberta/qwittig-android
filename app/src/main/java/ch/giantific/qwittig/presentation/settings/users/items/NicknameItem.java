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
 * Created by fabio on 07.02.16.
 */
public class NicknameItem extends BaseObservable implements SettingsUsersItem {

    public static final Parcelable.Creator<NicknameItem> CREATOR = new Parcelable.Creator<NicknameItem>() {
        @Override
        public NicknameItem createFromParcel(Parcel source) {
            return new NicknameItem(source);
        }

        @Override
        public NicknameItem[] newArray(int size) {
            return new NicknameItem[size];
        }
    };
    private AddListener mAddListener;
    private String mNickname;
    private boolean mValidate;

    public NicknameItem() {
    }

    protected NicknameItem(Parcel in) {
        mNickname = in.readString();
        mValidate = in.readByte() != 0;
    }

    public void setAddListener(@NonNull AddListener addListener) {
        mAddListener = addListener;
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
    public boolean isValidate() {
        return mValidate;
    }

    public void setValidate(boolean validate) {
        mValidate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(mNickname);
    }

    public void onNicknameChanged(CharSequence s, int start, int before, int count) {
        mNickname = s.toString();
        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    public void onAddUserClick(View view) {
        if (validate()) {
            mAddListener.onValidUserEntered(mNickname);
        }
    }

    private boolean validate() {
        setValidate(true);
        return isNicknameComplete();
    }

    @Override
    public int getType() {
        return Type.NICKNAME;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mNickname);
        dest.writeByte(mValidate ? (byte) 1 : (byte) 0);
    }

    public interface AddListener {
        void onValidUserEntered(@NonNull String nickname);
    }
}
