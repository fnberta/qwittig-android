/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.os.Parcel;
import android.support.annotation.NonNull;

import java.util.Objects;

/**
 * Provides a row filled with users that belong to a list item in the add or edit purchase screen.
 */
public class PurchaseAddEditItemUsers implements PurchaseAddEditItemModel {

    public static final Creator<PurchaseAddEditItemUsers> CREATOR = new Creator<PurchaseAddEditItemUsers>() {
        @Override
        public PurchaseAddEditItemUsers createFromParcel(Parcel source) {
            return new PurchaseAddEditItemUsers(source);
        }

        @Override
        public PurchaseAddEditItemUsers[] newArray(int size) {
            return new PurchaseAddEditItemUsers[size];
        }
    };
    private PurchaseAddEditItemUsersUser[] mUsers;

    public PurchaseAddEditItemUsers(@NonNull PurchaseAddEditItemUsersUser[] users) {
        mUsers = users;
    }

    private PurchaseAddEditItemUsers(Parcel in) {
        mUsers = in.createTypedArray(PurchaseAddEditItemUsersUser.CREATOR);
    }

    public PurchaseAddEditItemUsersUser[] getUsers() {
        return mUsers;
    }

    public void setUsers(@NonNull PurchaseAddEditItemUsersUser[] users) {
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
