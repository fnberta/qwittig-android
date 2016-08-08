/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Provides a row filled with users that belong to a list item in the add or edit purchase screen.
 */
public class PurchaseAddEditItemIdentities implements PurchaseAddEditItemModel {

    public static final Creator<PurchaseAddEditItemIdentities> CREATOR = new Creator<PurchaseAddEditItemIdentities>() {
        @Override
        public PurchaseAddEditItemIdentities createFromParcel(Parcel source) {
            return new PurchaseAddEditItemIdentities(source);
        }

        @Override
        public PurchaseAddEditItemIdentities[] newArray(int size) {
            return new PurchaseAddEditItemIdentities[size];
        }
    };
    private PurchaseAddEditItemIdentity[] mUsers;

    public PurchaseAddEditItemIdentities(@NonNull PurchaseAddEditItemIdentity[] users) {
        mUsers = users;
    }

    private PurchaseAddEditItemIdentities(Parcel in) {
        mUsers = in.createTypedArray(PurchaseAddEditItemIdentity.CREATOR);
    }

    public PurchaseAddEditItemIdentity[] getUsers() {
        return mUsers;
    }

    public void setUsers(@NonNull PurchaseAddEditItemIdentity[] users) {
        mUsers = users;
    }

    @Override
    public int getType() {
        return Type.USERS;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(mUsers, 0);
    }
}
