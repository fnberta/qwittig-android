/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.EmptyViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.LoadingViewModel;
import ch.giantific.qwittig.presentation.common.viewmodels.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.purchases.details.PurchaseDetailsContract;

/**
 * Provides an implementation of the {@link PurchaseDetailsContract}.
 */
public class PurchaseDetailsViewModel extends BaseObservable
        implements LoadingViewModel, EmptyViewModel, PurchaseReceiptViewModel, Parcelable {

    public static final Parcelable.Creator<PurchaseDetailsViewModel> CREATOR = new Parcelable.Creator<PurchaseDetailsViewModel>() {
        @Override
        public PurchaseDetailsViewModel createFromParcel(Parcel source) {
            return new PurchaseDetailsViewModel(source);
        }

        @Override
        public PurchaseDetailsViewModel[] newArray(int size) {
            return new PurchaseDetailsViewModel[size];
        }
    };
    private boolean loading;
    private boolean empty;
    private String store;
    private String date;
    private String receipt;
    private String total;
    private String totalForeign;
    private String myShare;
    private String myShareForeign;
    private String note;
    private double exchangeRate;

    public PurchaseDetailsViewModel(boolean loading) {
        this.empty = true;
        this.loading = loading;
    }

    private PurchaseDetailsViewModel(Parcel in) {
        this.loading = in.readByte() != 0;
        this.empty = in.readByte() != 0;
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
    public String getReceipt() {
        return receipt;
    }

    @Override
    public void setReceipt(@NonNull String receiptUrl) {
        receipt = receiptUrl;
        notifyPropertyChanged(BR.receipt);
        notifyPropertyChanged(BR.receiptAvailable);
    }

    @Override
    @Bindable
    public boolean isReceiptAvailable() {
        return !TextUtils.isEmpty(receipt);
    }

    @Bindable
    public String getStore() {
        return store;
    }

    public void setStore(@NonNull String store) {
        this.store = store;
        notifyPropertyChanged(BR.store);
    }

    @Bindable
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }

    @Bindable
    public String getTotal() {
        return total;
    }

    public void setTotal(@NonNull String total) {
        this.total = total;
        notifyPropertyChanged(BR.total);
    }

    @Bindable
    public String getTotalForeign() {
        return totalForeign;
    }

    public void setTotalForeign(@NonNull String totalForeign) {
        this.totalForeign = totalForeign;
        notifyPropertyChanged(BR.totalForeign);
    }

    @Bindable
    public String getMyShare() {
        return myShare;
    }

    public void setMyShare(@NonNull String myShare) {
        this.myShare = myShare;
        notifyPropertyChanged(BR.myShare);
    }

    @Bindable
    public String getMyShareForeign() {
        return myShareForeign;
    }

    public void setMyShareForeign(@NonNull String myShareForeign) {
        this.myShareForeign = myShareForeign;
        notifyPropertyChanged(BR.myShareForeign);
    }

    @Bindable
    public String getNote() {
        return note;
    }

    public void setNote(@NonNull String note) {
        this.note = note;
        notifyPropertyChanged(BR.note);
        notifyPropertyChanged(BR.noteAvailable);
    }

    @Bindable
    public boolean isNoteAvailable() {
        return !TextUtils.isEmpty(note);
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.loading ? (byte) 1 : (byte) 0);
        dest.writeByte(this.empty ? (byte) 1 : (byte) 0);
    }
}
