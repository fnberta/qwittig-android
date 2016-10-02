package ch.giantific.qwittig.presentation.finance.unpaid.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.EmptyViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;

/**
 * Created by fabio on 29.09.16.
 */

public class CompsUnpaidViewModel extends BaseObservable implements Parcelable,
        LoadingViewModel, EmptyViewModel {

    public static final Creator<CompsUnpaidViewModel> CREATOR = new Creator<CompsUnpaidViewModel>() {
        @Override
        public CompsUnpaidViewModel createFromParcel(Parcel source) {
            return new CompsUnpaidViewModel(source);
        }

        @Override
        public CompsUnpaidViewModel[] newArray(int size) {
            return new CompsUnpaidViewModel[size];
        }
    };
    private boolean empty;
    private boolean loading;

    public CompsUnpaidViewModel(boolean loading) {
        this.empty = true;
        this.loading = loading;
    }

    private CompsUnpaidViewModel(Parcel in) {
        this.empty = in.readByte() != 0;
        this.loading = in.readByte() != 0;
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void setEmpty(boolean empty) {
        this.empty = empty;
        notifyPropertyChanged(BR.empty);
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
        dest.writeByte(this.empty ? (byte) 1 : (byte) 0);
        dest.writeByte(this.loading ? (byte) 1 : (byte) 0);
    }
}
