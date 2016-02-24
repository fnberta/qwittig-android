/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.items;

import android.databinding.BaseObservable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Provides a list item with no content for the add or edit purchase screen. The content is
 * delivered through the screen's main view model.
 */
public class AddEditPurchaseGenericItem extends BaseObservable
        implements AddEditPurchaseBaseItem, Parcelable {

    public static final Creator<AddEditPurchaseGenericItem> CREATOR = new Creator<AddEditPurchaseGenericItem>() {
        @Override
        public AddEditPurchaseGenericItem createFromParcel(Parcel source) {
            return new AddEditPurchaseGenericItem(source);
        }

        @Override
        public AddEditPurchaseGenericItem[] newArray(int size) {
            return new AddEditPurchaseGenericItem[size];
        }
    };
    @Type
    private final int mType;

    private AddEditPurchaseGenericItem(@Type int type) {
        mType = type;
    }

    @SuppressWarnings("WrongConstant")
    private AddEditPurchaseGenericItem(Parcel in) {
        mType = in.readInt();
    }

    public static AddEditPurchaseGenericItem createNewDateInstance() {
        return new AddEditPurchaseGenericItem(Type.DATE);
    }

    public static AddEditPurchaseGenericItem createNewStoreInstance() {
        return new AddEditPurchaseGenericItem(Type.STORE);
    }

    public static AddEditPurchaseGenericItem createNewAddRowInstance() {
        return new AddEditPurchaseGenericItem(Type.ADD_ROW);
    }

    public static AddEditPurchaseGenericItem createNewTotalInstance() {
        return new AddEditPurchaseGenericItem(Type.TOTAL);
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
