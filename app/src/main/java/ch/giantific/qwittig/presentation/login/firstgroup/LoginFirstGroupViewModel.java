package ch.giantific.qwittig.presentation.login.firstgroup;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;

/**
 * Created by fabio on 13.05.16.
 */
public class LoginFirstGroupViewModel extends BaseObservable
        implements Parcelable, LoadingViewModel {

    public static final Creator<LoginFirstGroupViewModel> CREATOR = new Creator<LoginFirstGroupViewModel>() {
        @Override
        public LoginFirstGroupViewModel createFromParcel(Parcel in) {
            return new LoginFirstGroupViewModel(in);
        }

        @Override
        public LoginFirstGroupViewModel[] newArray(int size) {
            return new LoginFirstGroupViewModel[size];
        }
    };
    public final ObservableField<String> groupName = new ObservableField<>();
    private boolean loading;
    private boolean validate;
    private String groupCurrency;
    private int selectedGroupCurrency;

    public LoginFirstGroupViewModel(boolean loading) {
        this.loading = loading;

        addChangedListeners();
    }

    private LoginFirstGroupViewModel(Parcel in) {
        loading = in.readByte() != 0;
        validate = in.readByte() != 0;
        groupName.set(in.readString());
        groupCurrency = in.readString();
        selectedGroupCurrency = in.readInt();

        addChangedListeners();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeByte((byte) (validate ? 1 : 0));
        dest.writeString(groupName.get());
        dest.writeString(groupCurrency);
        dest.writeInt(selectedGroupCurrency);
    }

    private void addChangedListeners() {
        groupName.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (validate) {
                    notifyPropertyChanged(BR.groupNameComplete);
                }
            }
        });
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

    @Bindable
    public boolean isGroupNameComplete() {
        return !TextUtils.isEmpty(groupName.get());
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

}
