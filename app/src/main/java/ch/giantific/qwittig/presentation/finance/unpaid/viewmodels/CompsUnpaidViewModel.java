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
        public CompsUnpaidViewModel createFromParcel(Parcel in) {
            return new CompsUnpaidViewModel(in);
        }

        @Override
        public CompsUnpaidViewModel[] newArray(int size) {
            return new CompsUnpaidViewModel[size];
        }
    };
    public static final String TAG = CompsUnpaidViewModel.class.getCanonicalName();
    private boolean empty;
    private boolean loading;
    private String confirmingId;

    public CompsUnpaidViewModel() {
        this.empty = true;
        this.loading = true;
    }

    private CompsUnpaidViewModel(Parcel in) {
        empty = in.readByte() != 0;
        loading = in.readByte() != 0;
        confirmingId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (empty ? 1 : 0));
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeString(confirmingId);
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

    public String getConfirmingId() {
        return confirmingId;
    }

    public void setConfirmingId(String confirmingId) {
        this.confirmingId = confirmingId;
    }
}
