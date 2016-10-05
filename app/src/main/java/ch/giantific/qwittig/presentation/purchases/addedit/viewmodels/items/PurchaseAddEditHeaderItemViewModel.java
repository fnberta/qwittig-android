/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.presentation.common.viewmodels.items.HeaderItemViewModel;

/**
 * Provides a header row in the add or edit purchase screen.
 */
public class PurchaseAddEditHeaderItemViewModel extends BaseObservable
        implements HeaderItemViewModel, BasePurchaseAddEditItemViewModel {

    public static final Creator<PurchaseAddEditHeaderItemViewModel> CREATOR = new Creator<PurchaseAddEditHeaderItemViewModel>() {
        @Override
        public PurchaseAddEditHeaderItemViewModel createFromParcel(Parcel in) {
            return new PurchaseAddEditHeaderItemViewModel(in);
        }

        @Override
        public PurchaseAddEditHeaderItemViewModel[] newArray(int size) {
            return new PurchaseAddEditHeaderItemViewModel[size];
        }
    };
    @StringRes
    private int header;

    public PurchaseAddEditHeaderItemViewModel(@StringRes int header) {
        this.header = header;
    }

    private PurchaseAddEditHeaderItemViewModel(Parcel in) {
        header = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(header);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    @Bindable
    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
        notifyPropertyChanged(BR.header);
    }

    @Override
    public int getViewType() {
        return ViewType.HEADER;
    }

}
