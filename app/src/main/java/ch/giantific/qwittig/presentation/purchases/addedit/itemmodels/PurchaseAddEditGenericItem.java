/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.databinding.BaseObservable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Provides a list item with no content for the add or edit purchase screen. The content is
 * delivered through the screen's main view model.
 */
public class PurchaseAddEditGenericItem extends BaseObservable
        implements PurchaseAddEditItemModel, Parcelable {

    public static final Creator<PurchaseAddEditGenericItem> CREATOR = new Creator<PurchaseAddEditGenericItem>() {
        @Override
        public PurchaseAddEditGenericItem createFromParcel(Parcel source) {
            return new PurchaseAddEditGenericItem(source);
        }

        @Override
        public PurchaseAddEditGenericItem[] newArray(int size) {
            return new PurchaseAddEditGenericItem[size];
        }
    };
    @Type
    private final int mType;

    private PurchaseAddEditGenericItem(@Type int type) {
        mType = type;
    }

    @SuppressWarnings("WrongConstant")
    private PurchaseAddEditGenericItem(Parcel in) {
        mType = in.readInt();
    }

    public static PurchaseAddEditGenericItem createNewDateInstance() {
        return new PurchaseAddEditGenericItem(Type.DATE);
    }

    public static PurchaseAddEditGenericItem createNewStoreInstance() {
        return new PurchaseAddEditGenericItem(Type.STORE);
    }

    public static PurchaseAddEditGenericItem createNewAddRowInstance() {
        return new PurchaseAddEditGenericItem(Type.ADD_ROW);
    }

    public static PurchaseAddEditGenericItem createNewTotalInstance() {
        return new PurchaseAddEditGenericItem(Type.TOTAL);
    }

    public int getType() {
        return mType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType);
    }
}
