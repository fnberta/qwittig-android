package ch.giantific.qwittig.presentation.login.firstgroup;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginFirstGroupViewModel extends BaseObservable implements Parcelable,
        LoadingViewModel {

    public static final Parcelable.Creator<LoginFirstGroupViewModel> CREATOR = new Parcelable.Creator<LoginFirstGroupViewModel>() {
        @Override
        public LoginFirstGroupViewModel createFromParcel(Parcel source) {
            return new LoginFirstGroupViewModel(source);
        }

        @Override
        public LoginFirstGroupViewModel[] newArray(int size) {
            return new LoginFirstGroupViewModel[size];
        }
    };
    private boolean loading;
    private boolean validate;
    private String groupName;
    private String groupCurrency;
    private int selectedGroupCurrency;

    public LoginFirstGroupViewModel(boolean loading) {
        this.loading = loading;
    }

    private LoginFirstGroupViewModel(Parcel in) {
        this.loading = in.readByte() != 0;
        this.validate = in.readByte() != 0;
        this.groupName = in.readString();
        this.groupCurrency = in.readString();
        this.selectedGroupCurrency = in.readInt();
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
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(@NonNull String groupName) {
        this.groupName = groupName;
        notifyPropertyChanged(BR.groupName);
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Bindable
    public boolean isGroupNameComplete() {
        return !TextUtils.isEmpty(groupName);
    }

    public String getGroupCurrency() {
        return groupCurrency;
    }

    public void setGroupCurrency(@NonNull String groupCurrency) {
        this.groupCurrency = groupCurrency;
    }

    @Bindable
    public int getSelectedGroupCurrency() {
        return selectedGroupCurrency;
    }

    public void setSelectedGroupCurrency(int selectedGroupCurrency) {
        this.selectedGroupCurrency = selectedGroupCurrency;
        notifyPropertyChanged(BR.selectedGroupCurrency);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.loading ? (byte) 1 : (byte) 0);
        dest.writeByte(this.validate ? (byte) 1 : (byte) 0);
        dest.writeString(this.groupName);
        dest.writeString(this.groupCurrency);
        dest.writeInt(this.selectedGroupCurrency);
    }
}
