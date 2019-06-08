/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.viewmodels.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;

/**
 * Provides a purchase list item in the addItemAtPosition or edit purchase screen.
 */
public class PurchaseAddEditArticleItemViewModel extends BaseObservable
        implements BasePurchaseAddEditItemViewModel {

    public static final Creator<PurchaseAddEditArticleItemViewModel> CREATOR = new Creator<PurchaseAddEditArticleItemViewModel>() {
        @Override
        public PurchaseAddEditArticleItemViewModel createFromParcel(Parcel in) {
            return new PurchaseAddEditArticleItemViewModel(in);
        }

        @Override
        public PurchaseAddEditArticleItemViewModel[] newArray(int size) {
            return new PurchaseAddEditArticleItemViewModel[size];
        }
    };
    public final ObservableField<String> name = new ObservableField<>();
    private String price;
    private double priceParsed;
    private boolean validate;
    private PurchaseAddEditArticleIdentityItemViewModel[] identities;

    public PurchaseAddEditArticleItemViewModel(@NonNull PurchaseAddEditArticleIdentityItemViewModel[] identities) {
        this("", "", 0, identities);
    }


    public PurchaseAddEditArticleItemViewModel(@NonNull String name,
                                               @NonNull String price,
                                               double priceParsed,
                                               @NonNull PurchaseAddEditArticleIdentityItemViewModel[] identities) {
        this.name.set(name);
        this.price = price;
        this.priceParsed = priceParsed;
        this.identities = identities;

        addChangedListeners();
    }

    private PurchaseAddEditArticleItemViewModel(Parcel in) {
        validate = in.readByte() != 0;
        name.set(in.readString());
        price = in.readString();
        priceParsed = in.readDouble();
        identities = in.createTypedArray(PurchaseAddEditArticleIdentityItemViewModel.CREATOR);

        addChangedListeners();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (validate ? 1 : 0));
        dest.writeString(name.get());
        dest.writeString(price);
        dest.writeDouble(priceParsed);
        dest.writeTypedArray(identities, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void addChangedListeners() {
        name.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                if (validate) {
                    notifyPropertyChanged(BR.nameComplete);
                }
            }
        });
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
    public boolean isNameComplete() {
        return !TextUtils.isEmpty(name.get());
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
            notifyPropertyChanged(BR.priceComplete);
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
        for (PurchaseAddEditArticleIdentityItemViewModel identity : identities) {
            if (Objects.equals(identity.getIdentityId(), identityItem.getIdentityId())) {
                identity.setSelected(isSelected);
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

}
