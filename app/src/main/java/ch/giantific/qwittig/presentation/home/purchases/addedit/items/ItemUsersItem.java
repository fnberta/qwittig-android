/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.items;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Created by fabio on 11.02.16.
 */
public class ItemUsersItem implements AddEditItem {

    public static final Creator<ItemUsersItem> CREATOR = new Creator<ItemUsersItem>() {
        @Override
        public ItemUsersItem createFromParcel(Parcel source) {
            return new ItemUsersItem(source);
        }

        @Override
        public ItemUsersItem[] newArray(int size) {
            return new ItemUsersItem[size];
        }
    };
    private ItemUsersItemUser[] mUsers;

    public ItemUsersItem(@NonNull ItemUsersItemUser[] users) {
        mUsers = users;
    }

    protected ItemUsersItem(Parcel in) {
        mUsers = (ItemUsersItemUser[]) in.readParcelableArray(ItemUsersItemUser.class.getClassLoader());
    }

    public ItemUsersItemUser[] getUsers() {
        return mUsers;
    }

    public void setUsers(@NonNull ItemUsersItemUser[] users) {
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
        dest.writeParcelableArray(mUsers, 0);
    }
}
