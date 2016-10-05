/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.EmptyViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersContract;

/**
 * Provides an implementation of the {@link SettingsUsersContract}.
 */
public class SettingsUsersViewModel extends BaseObservable
        implements Parcelable, EmptyViewModel {

    public static final Creator<SettingsUsersViewModel> CREATOR = new Creator<SettingsUsersViewModel>() {
        @Override
        public SettingsUsersViewModel createFromParcel(Parcel in) {
            return new SettingsUsersViewModel(in);
        }

        @Override
        public SettingsUsersViewModel[] newArray(int size) {
            return new SettingsUsersViewModel[size];
        }
    };
    private boolean empty;
    private String groupName;
    private String nickname;
    private boolean validate;

    public SettingsUsersViewModel() {
        this.empty = true;
    }

    private SettingsUsersViewModel(Parcel in) {
        empty = in.readByte() != 0;
        groupName = in.readString();
        nickname = in.readString();
        validate = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (empty ? 1 : 0));
        dest.writeString(groupName);
        dest.writeString(nickname);
        dest.writeByte((byte) (validate ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void setEmpty(boolean empty) {
        this.empty = empty;
        notifyPropertyChanged(BR.empty);
    }

    @Bindable
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(@NonNull String groupName) {
        this.groupName = groupName;
        notifyPropertyChanged(BR.groupName);
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    public void setNickname(@NonNull String nickname) {
        this.nickname = nickname;
        notifyPropertyChanged(BR.nickname);
        if (validate) {
            notifyPropertyChanged(BR.nicknameComplete);
        }
    }

    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(nickname);
    }

    @Bindable
    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    public boolean isInputValid() {
        setValidate(true);
        return isNicknameComplete();
    }

}
