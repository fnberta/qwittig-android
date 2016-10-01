/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.PurchaseReceiptViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditDateItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditStoreItemViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items.PurchaseAddEditTotalItemViewModel;

public class PurchaseAddEditViewModel extends BaseObservable
        implements PurchaseReceiptViewModel,
        PurchaseAddEditDateItemViewModel,
        PurchaseAddEditStoreItemViewModel,
        PurchaseAddEditTotalItemViewModel,
        Parcelable {

    public static final Creator<PurchaseAddEditViewModel> CREATOR = new Creator<PurchaseAddEditViewModel>() {
        @Override
        public PurchaseAddEditViewModel createFromParcel(Parcel source) {
            return new PurchaseAddEditViewModel(source);
        }

        @Override
        public PurchaseAddEditViewModel[] newArray(int size) {
            return new PurchaseAddEditViewModel[size];
        }
    };
    private final List<String> supportedCurrencies;
    private boolean loading;
    private String receipt;
    private String currency;
    private String note;
    private Date date;
    private String dateFormatted;
    private String store;
    private double exchangeRate;
    private String exchangeRateFormatted;
    private double total;
    private String totalFormatted;
    private String myShare;

    public PurchaseAddEditViewModel(@NonNull List<String> supportedCurrencies,
                                    boolean loading,
                                    @NonNull Date date,
                                    @NonNull String dateFormatted) {
        this.supportedCurrencies = supportedCurrencies;
        this.loading = loading;
        this.date = date;
        this.dateFormatted = dateFormatted;
        this.exchangeRate = 1;
    }

    private PurchaseAddEditViewModel(Parcel in) {
        this.supportedCurrencies = in.createStringArrayList();
        this.loading = in.readByte() != 0;
        this.receipt = in.readString();
        this.currency = in.readString();
        this.note = in.readString();
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
        this.dateFormatted = in.readString();
        this.store = in.readString();
        this.exchangeRate = in.readDouble();
        this.exchangeRateFormatted = in.readString();
        this.total = in.readDouble();
        this.totalFormatted = in.readString();
        this.myShare = in.readString();
    }

    public List<String> getSupportedCurrencies() {
        return supportedCurrencies;
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
    public String getReceipt() {
        return receipt;
    }

    @Override
    public void setReceipt(@NonNull String receipt) {
        this.receipt = receipt;
        notifyPropertyChanged(BR.receipt);
        notifyPropertyChanged(BR.receiptAvailable);
    }

    @Override
    @Bindable
    public boolean isReceiptAvailable() {
        return !TextUtils.isEmpty(receipt);
    }

    @Bindable
    public String getNote() {
        return note;
    }

    public void setNote(@NonNull String note) {
        this.note = note;
        notifyPropertyChanged(BR.note);
    }

    public boolean isNoteAvailable() {
        return !TextUtils.isEmpty(note);
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    @Bindable
    public String getDateFormatted() {
        return dateFormatted;
    }

    @Override
    public void setDate(@NonNull Date date, @NonNull String dateFormatted) {
        this.date = date;
        this.dateFormatted = dateFormatted;
        notifyPropertyChanged(BR.dateFormatted);
    }

    @Bindable
    public String getStore() {
        return store;
    }

    @Override
    public void setStore(@NonNull String store) {
        this.store = store;
        notifyPropertyChanged(BR.store);
    }

    @Override
    public double getTotal() {
        return total;
    }

    @Bindable
    public String getTotalFormatted() {
        return totalFormatted;
    }

    @Override
    public void setTotal(double total, @NonNull String totalFormatted) {
        this.total = total;
        this.totalFormatted = totalFormatted;
        notifyPropertyChanged(BR.totalFormatted);
    }

    @Override
    @Bindable
    public String getMyShare() {
        return myShare;
    }

    @Override
    public void setMyShare(@NonNull String myShareFormatted) {
        this.myShare = myShareFormatted;
        notifyPropertyChanged(BR.myShare);
    }

    @Override
    @Bindable
    public String getCurrency() {
        return currency;
    }

    @Override
    public void setCurrency(@NonNull String currency, boolean notify) {
        this.currency = currency;
        notifyPropertyChanged(BR.currency);
        if (notify) {
            notifyPropertyChanged(BR.currencySelected);
        }
    }

    @Override
    @Bindable
    public int getCurrencySelected() {
        return supportedCurrencies.indexOf(currency);
    }

    @Override
    @Bindable
    public double getExchangeRate() {
        return exchangeRate;
    }

    @Override
    @Bindable
    public String getExchangeRateFormatted() {
        return exchangeRateFormatted;
    }

    @Override
    public void setExchangeRate(double exchangeRate, @NonNull String exchangeRateFormatted) {
        this.exchangeRate = exchangeRate;
        this.exchangeRateFormatted = exchangeRateFormatted;
        notifyPropertyChanged(BR.exchangeRateFormatted);
        notifyPropertyChanged(BR.exchangeRateVisible);
    }

    @Override
    @Bindable
    public boolean isExchangeRateVisible() {
        return exchangeRate != 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.supportedCurrencies);
        dest.writeByte(this.loading ? (byte) 1 : (byte) 0);
        dest.writeString(this.receipt);
        dest.writeString(this.currency);
        dest.writeString(this.note);
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
        dest.writeString(this.dateFormatted);
        dest.writeString(this.store);
        dest.writeDouble(this.exchangeRate);
        dest.writeString(this.exchangeRateFormatted);
        dest.writeDouble(this.total);
        dest.writeString(this.totalFormatted);
        dest.writeString(this.myShare);
    }
}