/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.items;

import android.databinding.BaseObservable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fabio on 30.01.16.
 */
public class GenericItem extends BaseObservable
        implements AddEditItem, Parcelable {

    public static final Creator<GenericItem> CREATOR = new Creator<GenericItem>() {
        @Override
        public GenericItem createFromParcel(Parcel source) {
            return new GenericItem(source);
        }

        @Override
        public GenericItem[] newArray(int size) {
            return new GenericItem[size];
        }
    };
    @Type
    private int mType;

    private GenericItem(@Type int type) {
        mType = type;
    }

    @SuppressWarnings("WrongConstant")
    protected GenericItem(Parcel in) {
        mType = in.readInt();
    }

    public static GenericItem createNewDateInstance() {
        return new GenericItem(Type.DATE);
    }

    public static GenericItem createNewStoreInstance() {
        return new GenericItem(Type.STORE);
    }

    public static GenericItem createNewAddRowInstance() {
        return new GenericItem(Type.ADD_ROW);
    }

    public static GenericItem createNewTotalInstance() {
        return new GenericItem(Type.TOTAL);
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
