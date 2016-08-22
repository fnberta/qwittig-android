/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Provides a row filled with identities that belong to a list item in the add or edit purchase screen.
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
    private PurchaseAddEditItemIdentity[] identities;

    public PurchaseAddEditItemIdentities(@NonNull PurchaseAddEditItemIdentity[] identities) {
        this.identities = identities;
    }

    private PurchaseAddEditItemIdentities(Parcel in) {
        identities = in.createTypedArray(PurchaseAddEditItemIdentity.CREATOR);
    }

    public PurchaseAddEditItemIdentity[] getIdentities() {
        return identities;
    }

    public void setIdentities(@NonNull PurchaseAddEditItemIdentity[] identities) {
        this.identities = identities;
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
        dest.writeTypedArray(identities, 0);
    }
}
