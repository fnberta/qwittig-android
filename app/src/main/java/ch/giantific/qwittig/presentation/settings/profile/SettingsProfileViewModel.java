/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.utils.Utils;

/**
 * Provides an implementation of the {@link SettingsProfileContract}.
 */
public class SettingsProfileViewModel extends BaseObservable implements Parcelable {

    public static final Parcelable.Creator<SettingsProfileViewModel> CREATOR = new Parcelable.Creator<SettingsProfileViewModel>() {
        @Override
        public SettingsProfileViewModel createFromParcel(Parcel source) {
            return new SettingsProfileViewModel(source);
        }

        @Override
        public SettingsProfileViewModel[] newArray(int size) {
            return new SettingsProfileViewModel[size];
        }
    };
    private boolean validate;
    private String avatar;
    private String nickname;
    private String email;
    private String password;
    private String passwordRepeat;
    private boolean facebookUser;
    private boolean googleUser;
    private boolean unlinkSocialLogin;

    public SettingsProfileViewModel() {
    }

    private SettingsProfileViewModel(Parcel in) {
        this.validate = in.readByte() != 0;
        this.avatar = in.readString();
        this.nickname = in.readString();
        this.email = in.readString();
        this.password = in.readString();
        this.passwordRepeat = in.readString();
        this.facebookUser = in.readByte() != 0;
        this.googleUser = in.readByte() != 0;
        this.unlinkSocialLogin = in.readByte() != 0;
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
        return isEmailValid() && isPasswordValid() && isPasswordEqual() && isNicknameComplete();
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(@NonNull String avatarUrl) {
        avatar = avatarUrl;
        notifyPropertyChanged(BR.avatar);
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    public void setNickname(@NonNull String nickname) {
        this.nickname = nickname;
        notifyPropertyChanged(BR.nickname);
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Bindable
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(nickname);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Bindable
    public boolean isEmailValid() {
        return Utils.isEmailValid(email);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Bindable
    public boolean isPasswordValid() {
        return TextUtils.isEmpty(password) || Utils.isPasswordValid(password);
    }

    @Bindable
    public boolean isPasswordEqual() {
        return Objects.equals(password, passwordRepeat);
    }

    public boolean isFacebookUser() {
        return facebookUser;
    }

    public void setFacebookUser(boolean facebookUser) {
        this.facebookUser = facebookUser;
        notifyPropertyChanged(BR.emailAndPasswordVisible);
    }

    public boolean isGoogleUser() {
        return googleUser;
    }

    public void setGoogleUser(boolean googleUser) {
        this.googleUser = googleUser;
        notifyPropertyChanged(BR.emailAndPasswordVisible);
    }

    public boolean isUnlinkSocialLogin() {
        return unlinkSocialLogin;
    }

    public void setUnlinkSocialLogin(boolean unlinkSocialLogin) {
        this.unlinkSocialLogin = unlinkSocialLogin;
        notifyPropertyChanged(BR.emailAndPasswordVisible);
    }

    @Bindable
    public boolean isEmailAndPasswordVisible() {
        return !facebookUser && !googleUser || unlinkSocialLogin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.validate ? (byte) 1 : (byte) 0);
        dest.writeString(this.avatar);
        dest.writeString(this.nickname);
        dest.writeString(this.email);
        dest.writeString(this.password);
        dest.writeString(this.passwordRepeat);
        dest.writeByte(this.facebookUser ? (byte) 1 : (byte) 0);
        dest.writeByte(this.googleUser ? (byte) 1 : (byte) 0);
        dest.writeByte(this.unlinkSocialLogin ? (byte) 1 : (byte) 0);
    }
}