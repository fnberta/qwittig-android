/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.email;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableField;
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
        implements Parcelable, LoadingViewModel {

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
    public final ObservableField<String> email = new ObservableField<>();
    public final ObservableField<String> password = new ObservableField<>();
    public final ObservableField<String> passwordRepeat = new ObservableField<>();
    private boolean loading;
    private boolean signUp;
    private boolean validate;

    public LoginEmailViewModel(boolean loading) {
        this.loading = loading;

        addChangedListeners();
    }

    protected LoginEmailViewModel(Parcel in) {
        email.set(in.readString());
        password.set(in.readString());
        passwordRepeat.set(in.readString());
        loading = in.readByte() != 0;
        signUp = in.readByte() != 0;
        validate = in.readByte() != 0;

        addChangedListeners();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email.get());
        dest.writeString(password.get());
        dest.writeString(passwordRepeat.get());
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeByte((byte) (signUp ? 1 : 0));
        dest.writeByte((byte) (validate ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void addChangedListeners() {
        email.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (validate) {
                    notifyPropertyChanged(BR.emailValid);
                }
            }
        });
        final OnPropertyChangedCallback passwordCb = new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (validate) {
                    notifyPropertyChanged(BR.passwordValid);
                    notifyPropertyChanged(BR.passwordEqual);
                }
            }
        };
        password.addOnPropertyChangedCallback(passwordCb);
        passwordRepeat.addOnPropertyChangedCallback(passwordCb);
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

    @Bindable
    public boolean isEmailValid() {
        return !TextUtils.isEmpty(email.get());
    }

    @Bindable
    public boolean isPasswordValid() {
        return !TextUtils.isEmpty(password.get());
    }

    @Bindable
    public boolean isPasswordEqual() {
        return Objects.equals(password.get(), passwordRepeat.get());
    }

    @Bindable
    public boolean isSignUp() {
        return signUp;
    }

    public void setSignUp(boolean signUp) {
        this.signUp = signUp;
        notifyPropertyChanged(BR.signUp);
    }

    public boolean isInputValid(boolean forSignUp) {
        setValidate(true);
        return forSignUp
               ? isEmailValid() && isPasswordValid() && isPasswordEqual()
               : isEmailValid() && isPasswordValid();
    }

}
