/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items;

import android.databinding.BaseObservable;
import android.os.Parcel;

/**
 * Provides a list item with no content for the add or edit purchase screen. The content is
 * delivered through the screen's main view model.
 */
public class PurchaseAddEditGenericItemViewModel extends BaseObservable
        implements BasePurchaseAddEditItemViewModel {

    public static final Creator<PurchaseAddEditGenericItemViewModel> CREATOR = new Creator<PurchaseAddEditGenericItemViewModel>() {
        @Override
        public PurchaseAddEditGenericItemViewModel createFromParcel(Parcel in) {
            return new PurchaseAddEditGenericItemViewModel(in);
        }

        @Override
        public PurchaseAddEditGenericItemViewModel[] newArray(int size) {
            return new PurchaseAddEditGenericItemViewModel[size];
        }
    };
    @ViewType
    private final int type;

    private PurchaseAddEditGenericItemViewModel(@ViewType int type) {
        this.type = type;
    }

    @SuppressWarnings("WrongConstant")
    private PurchaseAddEditGenericItemViewModel(Parcel in) {
        type = in.readInt();
    }

    public static PurchaseAddEditGenericItemViewModel createNewDateInstance() {
        return new PurchaseAddEditGenericItemViewModel(ViewType.DATE);
    }

    public static PurchaseAddEditGenericItemViewModel createNewStoreInstance() {
        return new PurchaseAddEditGenericItemViewModel(ViewType.STORE);
    }

    public static PurchaseAddEditGenericItemViewModel createNewAddRowInstance() {
        return new PurchaseAddEditGenericItemViewModel(ViewType.ADD_ROW);
    }

    public static PurchaseAddEditGenericItemViewModel createNewTotalInstance() {
        return new PurchaseAddEditGenericItemViewModel(ViewType.TOTAL);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getViewType() {
        return type;
    }

}
