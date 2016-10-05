package ch.giantific.qwittig.presentation.finance.paid.viewmodels;

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

public class CompsPaidViewModel extends BaseObservable implements Parcelable,
        LoadingViewModel, EmptyViewModel {

    public static final Creator<CompsPaidViewModel> CREATOR = new Creator<CompsPaidViewModel>() {
        @Override
        public CompsPaidViewModel createFromParcel(Parcel in) {
            return new CompsPaidViewModel(in);
        }

        @Override
        public CompsPaidViewModel[] newArray(int size) {
            return new CompsPaidViewModel[size];
        }
    };
    private boolean empty;
    private boolean loading;

    public CompsPaidViewModel(boolean loading) {
        this.empty = true;
        this.loading = loading;
    }

    private CompsPaidViewModel(Parcel in) {
        empty = in.readByte() != 0;
        loading = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (empty ? 1 : 0));
        dest.writeByte((byte) (loading ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
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

}
