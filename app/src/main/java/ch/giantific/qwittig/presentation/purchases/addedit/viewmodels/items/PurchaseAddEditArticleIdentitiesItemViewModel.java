/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items;

import android.os.Parcel;
import android.support.annotation.NonNull;

/**
 * Provides a row filled with identities that belong to a list item in the addItemAtPosition or edit purchase screen.
 */
public class PurchaseAddEditArticleIdentitiesItemViewModel
        implements BasePurchaseAddEditItemViewModel {

    public static final Creator<PurchaseAddEditArticleIdentitiesItemViewModel> CREATOR = new Creator<PurchaseAddEditArticleIdentitiesItemViewModel>() {
        @Override
        public PurchaseAddEditArticleIdentitiesItemViewModel createFromParcel(Parcel in) {
            return new PurchaseAddEditArticleIdentitiesItemViewModel(in);
        }

        @Override
        public PurchaseAddEditArticleIdentitiesItemViewModel[] newArray(int size) {
            return new PurchaseAddEditArticleIdentitiesItemViewModel[size];
        }
    };
    private PurchaseAddEditArticleIdentityItemViewModel[] identities;

    public PurchaseAddEditArticleIdentitiesItemViewModel(@NonNull PurchaseAddEditArticleIdentityItemViewModel[] identities) {
        this.identities = identities;
    }

    private PurchaseAddEditArticleIdentitiesItemViewModel(Parcel in) {
        identities = in.createTypedArray(PurchaseAddEditArticleIdentityItemViewModel.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(identities, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public PurchaseAddEditArticleIdentityItemViewModel[] getIdentities() {
        return identities;
    }

    public void setIdentities(@NonNull PurchaseAddEditArticleIdentityItemViewModel[] identities) {
        this.identities = identities;
    }

    @Override
    public int getViewType() {
        return ViewType.IDENTITIES;
    }
}
