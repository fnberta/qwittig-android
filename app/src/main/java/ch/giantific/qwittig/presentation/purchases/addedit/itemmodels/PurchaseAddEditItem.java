/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;

/**
 * Provides a purchase list item in the add or edit purchase screen.
 */
public class PurchaseAddEditItem extends BaseObservable implements PurchaseAddEditItemModel {

    public static final Creator<PurchaseAddEditItem> CREATOR = new Creator<PurchaseAddEditItem>() {
        @Override
        public PurchaseAddEditItem createFromParcel(Parcel source) {
            return new PurchaseAddEditItem(source);
        }

        @Override
        public PurchaseAddEditItem[] newArray(int size) {
            return new PurchaseAddEditItem[size];
        }
    };
    private PriceChangedListener priceChangedListener;
    private NumberFormat moneyFormatter;
    private boolean validate;
    private String name;
    private String price;
    private PurchaseAddEditItemIdentity[] identities;

    public PurchaseAddEditItem(@NonNull PurchaseAddEditItemIdentity[] identities) {
        this("", "", identities);
    }

    public PurchaseAddEditItem(@NonNull String name, @NonNull String price,
                               @NonNull PurchaseAddEditItemIdentity[] identities) {
        this.name = name;
        this.price = price;
        this.identities = identities;
    }

    private PurchaseAddEditItem(Parcel in) {
        validate = in.readByte() != 0;
        name = in.readString();
        price = in.readString();
        identities = in.createTypedArray(PurchaseAddEditItemIdentity.CREATOR);
    }

    public void setPriceChangedListener(@NonNull PriceChangedListener priceChangedListener) {
        this.priceChangedListener = priceChangedListener;
    }

    public void setMoneyFormatter(@NonNull NumberFormat moneyFormatter) {
        this.moneyFormatter = moneyFormatter;
    }

    public void updatePriceFormat(@NonNull NumberFormat moneyFormatter) {
        setMoneyFormatter(moneyFormatter);
        setPrice(this.moneyFormatter.format(parsePrice()));
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getPrice() {
        return price;
    }

    public void setPrice(@NonNull String price) {
        this.price = price;
        notifyPropertyChanged(BR.price);
    }

    /**
     * Returns the price parsed as a double. Accounts for localized string formatting.
     *
     * @return the price parsed as a double.
     */
    public double parsePrice() {
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException e) {
            try {
                return moneyFormatter.parse(price).doubleValue();
            } catch (ParseException e1) {
                return 0;
            }
        }
    }

    @Bindable
    public PurchaseAddEditItemIdentity[] getIdentities() {
        return identities;
    }

    public void setIdentities(@NonNull PurchaseAddEditItemIdentity[] identities) {
        this.identities = identities;
    }

    public void toggleUser(@NonNull PurchaseAddEditItemIdentity userClicked) {
        final boolean isSelected = userClicked.isSelected();
        for (PurchaseAddEditItemIdentity user : identities) {
            if (Objects.equals(user.getIdentityId(), userClicked.getIdentityId())) {
                user.setSelected(isSelected);
            }
        }
    }

    @Bindable
    public boolean isValidate() {
        return validate;
    }

    @Bindable
    public boolean isNameComplete() {
        return !TextUtils.isEmpty(name);
    }

    @Bindable
    public boolean isPriceComplete() {
        return !TextUtils.isEmpty(price);
    }

    public void onNameChanged(CharSequence s, int start, int before, int count) {
        name = s.toString();

        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    public void onPriceChanged(CharSequence s, int start, int before, int count) {
        price = s.toString();
        priceChangedListener.onRowPriceChanged();

        if (validate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    public void onPriceFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            setPrice(moneyFormatter.format(parsePrice()));
        }
    }

    public boolean validateFields() {
        validate = true;
        notifyPropertyChanged(BR.validate);
        return isNameComplete() && isPriceComplete();
    }

    public List<String> getSelectedIdentitiesIds() {
        final List<String> userIds = new ArrayList<>();
        for (PurchaseAddEditItemIdentity user : identities) {
            if (user.isSelected()) {
                userIds.add(user.getIdentityId());
            }
        }
        return userIds;
    }

    @Override
    public int getType() {
        return Type.ITEM;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(validate ? (byte) 1 : (byte) 0);
        dest.writeString(name);
        dest.writeString(price);
        dest.writeTypedArray(identities, 0);
    }

    /**
     * Defines the action to take when the price changed.
     */
    public interface PriceChangedListener {

        /**
         * Called when the price in the row changes.
         */
        void onRowPriceChanged();
    }
}
