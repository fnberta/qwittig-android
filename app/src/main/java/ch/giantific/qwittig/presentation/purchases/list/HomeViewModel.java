/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;

/**
 * Provides an implementation of the {@link HomeContract}.
 */
public class HomeViewModel extends BaseObservable
        implements Parcelable {

    public static final Creator<HomeViewModel> CREATOR = new Creator<HomeViewModel>() {
        @Override
        public HomeViewModel createFromParcel(Parcel in) {
            return new HomeViewModel(in);
        }

        @Override
        public HomeViewModel[] newArray(int size) {
            return new HomeViewModel[size];
        }
    };
    public static final String TAG = HomeViewModel.class.getCanonicalName();
    private boolean draftsAvailable;
    private String ocrPurchaseId;

    public HomeViewModel() {
    }

    private HomeViewModel(Parcel in) {
        draftsAvailable = in.readByte() != 0;
        ocrPurchaseId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (draftsAvailable ? 1 : 0));
        dest.writeString(ocrPurchaseId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Bindable
    public boolean isDraftsAvailable() {
        return draftsAvailable;
    }

    public void setDraftsAvailable(boolean available) {
        draftsAvailable = available;
        notifyPropertyChanged(BR.draftsAvailable);
    }

    public String getOcrPurchaseId() {
        return ocrPurchaseId;
    }

    public void setOcrPurchaseId(@NonNull String ocrPurchaseId) {
        this.ocrPurchaseId = ocrPurchaseId;
        notifyPropertyChanged(BR.ocrAvailable);
    }

    @Bindable
    public boolean isOcrAvailable() {
        return !TextUtils.isEmpty(ocrPurchaseId);
    }

}
