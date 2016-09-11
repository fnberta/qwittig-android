/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Provides a row filled with identities that belong to a list item in the add or edit purchase screen.
 */
public class PurchaseAddEditArticleIdentitiesItem implements PurchaseAddEditItemModel {

    public static final Creator<PurchaseAddEditArticleIdentitiesItem> CREATOR = new Creator<PurchaseAddEditArticleIdentitiesItem>() {
        @Override
        public PurchaseAddEditArticleIdentitiesItem createFromParcel(Parcel source) {
            return new PurchaseAddEditArticleIdentitiesItem(source);
        }

        @Override
        public PurchaseAddEditArticleIdentitiesItem[] newArray(int size) {
            return new PurchaseAddEditArticleIdentitiesItem[size];
        }
    };
    private PurchaseAddEditArticleIdentity[] identities;

    public PurchaseAddEditArticleIdentitiesItem(@NonNull PurchaseAddEditArticleIdentity[] identities) {
        this.identities = identities;
    }

    private PurchaseAddEditArticleIdentitiesItem(Parcel in) {
        identities = in.createTypedArray(PurchaseAddEditArticleIdentity.CREATOR);
    }

    public PurchaseAddEditArticleIdentity[] getIdentities() {
        return identities;
    }

    public void setIdentities(@NonNull PurchaseAddEditArticleIdentity[] identities) {
        this.identities = identities;
    }

    @Override
    public int getType() {
        return Type.IDENTITIES;
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
