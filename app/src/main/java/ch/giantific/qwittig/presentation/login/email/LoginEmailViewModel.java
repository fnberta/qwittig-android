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
public class LoginEmailViewModel extends BaseObservable
        implements LoadingViewModel, Parcelable {

    public static final Creator<LoginEmailViewModel> CREATOR = new Creator<LoginEmailViewModel>() {
        @Override
        public LoginEmailViewModel createFromParcel(Parcel in) {
            return new LoginEmailViewModel(in);
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
        loading = in.readByte() != 0;
        signUp = in.readByte() != 0;
        email = in.readString();
        password = in.readString();
        passwordRepeat = in.readString();
        validate = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeByte((byte) (signUp ? 1 : 0));
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(passwordRepeat);
        dest.writeByte((byte) (validate ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
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
            notifyPropertyChanged(BR.emailComplete);
        }
    }

    @Bindable
    public boolean isPasswordComplete() {
        return !TextUtils.isEmpty(password);
    }

    @Bindable
    public boolean isPasswordEqual() {
        return Objects.equals(password, passwordRepeat);
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
        if (validate) {
            notifyPropertyChanged(BR.passwordEqual);
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
               ? isEmailComplete() && isPasswordComplete() && isPasswordEqual()
               : isEmailComplete() && isPasswordComplete();
    }

}
