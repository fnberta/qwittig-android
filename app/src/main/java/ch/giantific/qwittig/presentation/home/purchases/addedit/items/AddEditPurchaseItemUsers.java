/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.items;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Provides a row filled with users that belong to a list item in the add or edit purchase screen.
 */
public class AddEditPurchaseItemUsers implements AddEditPurchaseBaseItem {

    public static final Creator<AddEditPurchaseItemUsers> CREATOR = new Creator<AddEditPurchaseItemUsers>() {
        @Override
        public AddEditPurchaseItemUsers createFromParcel(Parcel source) {
            return new AddEditPurchaseItemUsers(source);
        }

        @Override
        public AddEditPurchaseItemUsers[] newArray(int size) {
            return new AddEditPurchaseItemUsers[size];
        }
    };
    private AddEditPurchaseItemUsersUser[] mUsers;

    public AddEditPurchaseItemUsers(@NonNull AddEditPurchaseItemUsersUser[] users) {
        mUsers = users;
    }

    private AddEditPurchaseItemUsers(Parcel in) {
        mUsers = in.createTypedArray(AddEditPurchaseItemUsersUser.CREATOR);
    }

    public AddEditPurchaseItemUsersUser[] getUsers() {
        return mUsers;
    }

    public void setUsers(@NonNull AddEditPurchaseItemUsersUser[] users) {
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
