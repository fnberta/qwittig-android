/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableField;
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
    public static final String TAG = SettingsProfileViewModel.class.getCanonicalName();
    public final ObservableField<String> nickname = new ObservableField<>();
    public final ObservableField<String> email = new ObservableField<>();
    public final ObservableField<String> passwordRepeat = new ObservableField<>();
    private boolean validate;
    private String avatar;
    private String password;
    private boolean facebookUser;
    private boolean googleUser;
    private boolean unlinkSocialLogin;

    public SettingsProfileViewModel() {
        addChangedListeners();
    }

    private SettingsProfileViewModel(Parcel in) {
        nickname.set(in.readString());
        email.set(in.readString());
        password = in.readString();
        passwordRepeat.set(in.readString());
        validate = in.readByte() != 0;
        avatar = in.readString();
        facebookUser = in.readByte() != 0;
        googleUser = in.readByte() != 0;
        unlinkSocialLogin = in.readByte() != 0;

        addChangedListeners();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nickname.get());
        dest.writeString(email.get());
        dest.writeString(password);
        dest.writeString(passwordRepeat.get());
        dest.writeByte((byte) (validate ? 1 : 0));
        dest.writeString(avatar);
        dest.writeByte((byte) (facebookUser ? 1 : 0));
        dest.writeByte((byte) (googleUser ? 1 : 0));
        dest.writeByte((byte) (unlinkSocialLogin ? 1 : 0));
    }

    private void addChangedListeners() {
        nickname.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (validate) {
                    notifyPropertyChanged(BR.nicknameComplete);
                }
            }
        });
        email.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (validate) {
                    notifyPropertyChanged(BR.emailValid);
                }
            }
        });
        passwordRepeat.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (validate) {
                    notifyPropertyChanged(BR.passwordValid);
                    notifyPropertyChanged(BR.passwordEqual);
                }
            }
        });
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
    public boolean isNicknameComplete() {
        return !TextUtils.isEmpty(nickname.get());
    }

    @Bindable
    public boolean isEmailValid() {
        return Utils.isEmailValid(email.get());
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
        if (validate) {
            notifyPropertyChanged(BR.passwordValid);
            notifyPropertyChanged(BR.passwordEqual);
        }
    }

    @Bindable
    public boolean isPasswordValid() {
        return TextUtils.isEmpty(password) || Utils.isPasswordValid(password);
    }

    @Bindable
    public boolean isPasswordEqual() {
        return Objects.equals(password, passwordRepeat.get());
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