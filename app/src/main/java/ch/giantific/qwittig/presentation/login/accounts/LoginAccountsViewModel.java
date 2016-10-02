/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.accounts;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;

public class LoginAccountsViewModel extends BaseObservable implements LoadingViewModel, Parcelable {

    public static final Parcelable.Creator<LoginAccountsViewModel> CREATOR = new Parcelable.Creator<LoginAccountsViewModel>() {
        @Override
        public LoginAccountsViewModel createFromParcel(Parcel source) {
            return new LoginAccountsViewModel(source);
        }

        @Override
        public LoginAccountsViewModel[] newArray(int size) {
            return new LoginAccountsViewModel[size];
        }
    };
    private boolean loading;

    public LoginAccountsViewModel(boolean loading) {
        this.loading = loading;
    }

    private LoginAccountsViewModel(Parcel in) {
        this.loading = in.readByte() != 0;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.loading ? (byte) 1 : (byte) 0);
    }
}
