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

public class LoginAccountsViewModel extends BaseObservable
        implements LoadingViewModel, Parcelable {

    public static final Creator<LoginAccountsViewModel> CREATOR = new Creator<LoginAccountsViewModel>() {
        @Override
        public LoginAccountsViewModel createFromParcel(Parcel in) {
            return new LoginAccountsViewModel(in);
        }

        @Override
        public LoginAccountsViewModel[] newArray(int size) {
            return new LoginAccountsViewModel[size];
        }
    };
    public static final String TAG = LoginAccountsViewModel.class.getCanonicalName();
    private boolean loading;
    private String joinIdentityId;

    public LoginAccountsViewModel() {
    }

    private LoginAccountsViewModel(Parcel in) {
        loading = in.readByte() != 0;
        joinIdentityId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeString(joinIdentityId);
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

    public String getJoinIdentityId() {
        return joinIdentityId;
    }

    public void setJoinIdentityId(String joinIdentityId) {
        this.joinIdentityId = joinIdentityId;
    }
}
