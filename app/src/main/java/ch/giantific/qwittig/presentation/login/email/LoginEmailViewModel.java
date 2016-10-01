/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.email;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;

/**
 * Provides an implementation of the {@link LoginEmailContract}.
 */
public class LoginEmailViewModel extends BaseObservable implements LoadingViewModel, Parcelable {

    public static final Parcelable.Creator<LoginEmailViewModel> CREATOR = new Parcelable.Creator<LoginEmailViewModel>() {
        @Override
        public LoginEmailViewModel createFromParcel(Parcel source) {
            return new LoginEmailViewModel(source);
        }

        @Override
        public LoginEmailViewModel[] newArray(int size) {
            return new LoginEmailViewModel[size];
        }
    };
    private boolean loading;
    private boolean signUp;
    private String email;
    private String password;
    private String passwordRepeat;
    private boolean validate;

    public LoginEmailViewModel(boolean loading) {
        this.loading = loading;
    }

    private LoginEmailViewModel(Parcel in) {
        this.loading = in.readByte() != 0;
        this.signUp = in.readByte() != 0;
        this.email = in.readString();
        this.password = in.readString();
        this.passwordRepeat = in.readString();
        this.validate = in.readByte() != 0;
    }

    @Override
    @Bindable
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
        notifyPropertyChanged(BR.loading);
    }

    @Bindable
    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Bindable
    public boolean isEmailComplete() {
        return !TextUtils.isEmpty(email);
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

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Bindable
    public boolean isSignUp() {
        return signUp;
    }

    public void setSignUp(boolean signUp) {
        this.signUp = signUp;
        notifyPropertyChanged(BR.signUp);
    }

    public boolean isInputValid() {
        setValidate(true);
        return signUp
               ? isEmailComplete() && isPasswordComplete() && Objects.equals(password, passwordRepeat)
               : isEmailComplete() && isPasswordComplete();
    }

    @Bindable
    public boolean isPasswordComplete() {
        return !TextUtils.isEmpty(password);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.loading ? (byte) 1 : (byte) 0);
        dest.writeByte(this.signUp ? (byte) 1 : (byte) 0);
        dest.writeString(this.email);
        dest.writeString(this.password);
        dest.writeString(this.passwordRepeat);
        dest.writeByte(this.validate ? (byte) 1 : (byte) 0);
    }
}
