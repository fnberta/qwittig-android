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
        public PurchaseAddEditViewModel createFromParcel(Parcel in) {
            return new PurchaseAddEditViewModel(in);
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
        supportedCurrencies = in.createStringArrayList();
        loading = in.readByte() != 0;
        receipt = in.readString();
        currency = in.readString();
        note = in.readString();
        dateFormatted = in.readString();
        store = in.readString();
        exchangeRate = in.readDouble();
        exchangeRateFormatted = in.readString();
        total = in.readDouble();
        totalFormatted = in.readString();
        myShare = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(supportedCurrencies);
        dest.writeByte((byte) (loading ? 1 : 0));
        dest.writeString(receipt);
        dest.writeString(currency);
        dest.writeString(note);
        dest.writeString(dateFormatted);
        dest.writeString(store);
        dest.writeDouble(exchangeRate);
        dest.writeString(exchangeRateFormatted);
        dest.writeDouble(total);
        dest.writeString(totalFormatted);
        dest.writeString(myShare);
    }

    @Override
    public int describeContents() {
        return 0;
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

}