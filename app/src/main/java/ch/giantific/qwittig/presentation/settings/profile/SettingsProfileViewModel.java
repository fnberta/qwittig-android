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
public class SettingsProfileViewModel extends BaseObservable
        implements Parcelable {

    public static final Creator<SettingsProfileViewModel> CREATOR = new Creator<SettingsProfileViewModel>() {
        @Override
        public SettingsProfileViewModel createFromParcel(Parcel in) {
            return new SettingsProfileViewModel(in);
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
        validate = in.readByte() != 0;
        avatar = in.readString();
        nickname = in.readString();
        email = in.readString();
        password = in.readString();
        passwordRepeat = in.readString();
        facebookUser = in.readByte() != 0;
        googleUser = in.readByte() != 0;
        unlinkSocialLogin = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (validate ? 1 : 0));
        dest.writeString(avatar);
        dest.writeString(nickname);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(passwordRepeat);
        dest.writeByte((byte) (facebookUser ? 1 : 0));
        dest.writeByte((byte) (googleUser ? 1 : 0));
        dest.writeByte((byte) (unlinkSocialLogin ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
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
            notifyPropertyChanged(BR.nicknameComplete);
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
            notifyPropertyChanged(BR.emailValid);
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
            notifyPropertyChanged(BR.passwordValid);
        }
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
        if (validate) {
            notifyPropertyChanged(BR.passwordEqual);
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

}