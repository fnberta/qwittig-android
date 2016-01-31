/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.viewmodels.rows.HeaderRowViewModel;

/**
 * Created by fabio on 30.01.16.
 */
public class PurchaseAddEditItem extends BaseObservable
        implements HeaderRowViewModel, Parcelable {

    public static final Parcelable.Creator<PurchaseAddEditItem> CREATOR = new Parcelable.Creator<PurchaseAddEditItem>() {
        public PurchaseAddEditItem createFromParcel(Parcel source) {
            return new PurchaseAddEditItem(source);
        }

        public PurchaseAddEditItem[] newArray(int size) {
            return new PurchaseAddEditItem[size];
        }
    };
    @Type
    private int mType;
    @StringRes
    private int mHeader;
    private RowItem mRowItem;
    private RowItemUser[] mRowItemUsers;

    private PurchaseAddEditItem(@Type int type, @StringRes int header) {
        mType = type;
        mHeader = header;
    }

    private PurchaseAddEditItem(@Type int type) {
        mType = type;
    }

    private PurchaseAddEditItem(@NonNull RowItem rowItem) {
        mType = Type.ITEM;
        mRowItem = rowItem;
    }

    public PurchaseAddEditItem(@NonNull RowItemUser[] users) {
        mType = Type.USERS;
        mRowItemUsers = users;
    }

    public PurchaseAddEditItem() {
        mType = Type.ADD_ROW;
    }

    @SuppressWarnings("WrongConstant")
    protected PurchaseAddEditItem(Parcel in) {
        mType = in.readInt();
        mHeader = in.readInt();
        mRowItem = in.readParcelable(RowItem.class.getClassLoader());
        mRowItemUsers = (RowItemUser[]) in.readParcelableArray(RowItemUser.class.getClassLoader());
    }

    public static PurchaseAddEditItem createNewHeaderInstance(@StringRes int header) {
        return new PurchaseAddEditItem(Type.HEADER, header);
    }

    public static PurchaseAddEditItem createNewDateInstance() {
        return new PurchaseAddEditItem(Type.DATE);
    }

    public static PurchaseAddEditItem createNewStoreInstance() {
        return new PurchaseAddEditItem(Type.STORE);
    }

    public static PurchaseAddEditItem createNewRowItemInstance(@NonNull RowItem rowItem) {
        return new PurchaseAddEditItem(rowItem);
    }

    public static PurchaseAddEditItem createNewUsersInstance(@NonNull RowItemUser[] users) {
        return new PurchaseAddEditItem(users);
    }

    public static PurchaseAddEditItem createNewAddRowInstance() {
        return new PurchaseAddEditItem();
    }

    public static PurchaseAddEditItem createNewTotalInstance() {
        return new PurchaseAddEditItem(Type.TOTAL);
    }

    public static PurchaseAddEditItem createNewExchangeRateInstance() {
        return new PurchaseAddEditItem(Type.EXCHANGE_RATE);
    }

    public int getType() {
        return mType;
    }

    @Bindable
    public int getHeader() {
        return mHeader;
    }

    public RowItem getRowItem() {
        return mRowItem;
    }

    public RowItemUser[] getRowItemUsers() {
        return mRowItemUsers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType);
        dest.writeInt(mHeader);
        dest.writeParcelable(mRowItem, 0);
        dest.writeParcelableArray(mRowItemUsers, 0);
    }

    @IntDef({Type.HEADER, Type.DATE, Type.STORE, Type.ITEM, Type.USERS, Type.ADD_ROW, Type.TOTAL,
            Type.EXCHANGE_RATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int HEADER = 0;
        int DATE = 1;
        int STORE = 2;
        int ITEM = 3;
        int USERS = 4;
        int ADD_ROW = 5;
        int TOTAL = 6;
        int EXCHANGE_RATE = 7;
    }
}
