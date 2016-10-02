/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;

/**
 * Provides a purchase list item in the add or edit purchase screen.
 */
public class PurchaseAddEditArticleItemViewModel extends BaseObservable
        implements BasePurchaseAddEditItemViewModel {

    public static final Creator<PurchaseAddEditArticleItemViewModel> CREATOR = new Creator<PurchaseAddEditArticleItemViewModel>() {
        @Override
        public PurchaseAddEditArticleItemViewModel createFromParcel(Parcel source) {
            return new PurchaseAddEditArticleItemViewModel(source);
        }

        @Override
        public PurchaseAddEditArticleItemViewModel[] newArray(int size) {
            return new PurchaseAddEditArticleItemViewModel[size];
        }
    };
    private boolean validate;
    private String name;
    private String price;
    private double priceParsed;
    private PurchaseAddEditArticleIdentityItemViewModel[] identities;

    public PurchaseAddEditArticleItemViewModel(@NonNull PurchaseAddEditArticleIdentityItemViewModel[] identities) {
        this("", "", identities);
    }

    public PurchaseAddEditArticleItemViewModel(@NonNull String name, @NonNull String price,
                                               @NonNull PurchaseAddEditArticleIdentityItemViewModel[] identities) {
        this.name = name;
        this.price = price;
        this.identities = identities;
    }

    private PurchaseAddEditArticleItemViewModel(Parcel in) {
        this.validate = in.readByte() != 0;
        this.name = in.readString();
        this.price = in.readString();
        this.priceParsed = in.readDouble();
        this.identities = in.createTypedArray(PurchaseAddEditArticleIdentityItemViewModel.CREATOR);
    }


    @Bindable
    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
        notifyPropertyChanged(BR.validate);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    @Bindable
    public boolean isNameComplete() {
        return !TextUtils.isEmpty(name);
    }

    @Bindable
    public String getPrice() {
        return price;
    }

    public void setPrice(@NonNull String price, double priceParsed) {
        this.price = price;
        this.priceParsed = priceParsed;
        notifyPropertyChanged(BR.price);
        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    public double getPriceParsed() {
        return priceParsed;
    }

    @Bindable
    public boolean isPriceComplete() {
        return !TextUtils.isEmpty(price);
    }

    @Bindable
    public PurchaseAddEditArticleIdentityItemViewModel[] getIdentities() {
        return identities;
    }

    public boolean isInputValid() {
        setValidate(true);
        return isNameComplete() && isPriceComplete();
    }

    public void toggleIdentity(@NonNull PurchaseAddEditArticleIdentityItemViewModel identityItem) {
        final boolean isSelected = identityItem.isSelected();
        for (PurchaseAddEditArticleIdentityItemViewModel user : identities) {
            if (Objects.equals(user.getIdentityId(), identityItem.getIdentityId())) {
                user.setSelected(isSelected);
            }
        }
    }

    public List<String> getSelectedIdentitiesIds() {
        final List<String> identityIds = new ArrayList<>();
        for (PurchaseAddEditArticleIdentityItemViewModel identityItem : identities) {
            if (identityItem.isSelected()) {
                identityIds.add(identityItem.getIdentityId());
            }
        }

        return identityIds;
    }

    @Override
    public int getViewType() {
        return ViewType.ARTICLE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.validate ? (byte) 1 : (byte) 0);
        dest.writeString(this.name);
        dest.writeString(this.price);
        dest.writeDouble(this.priceParsed);
        dest.writeTypedArray(this.identities, flags);
    }
}
