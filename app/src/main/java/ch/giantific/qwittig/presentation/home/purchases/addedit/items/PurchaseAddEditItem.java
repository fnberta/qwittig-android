/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit.items;

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

import ch.giantific.qwittig.BR;

/**
 * Provides a purchase list item in the add or edit purchase screen.
 */
public class PurchaseAddEditItem extends BaseObservable implements PurchaseAddEditBaseItem {

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
    private PriceChangedListener mPriceChangedListener;
    private NumberFormat mMoneyFormatter;
    private boolean mValidate;
    private String mName;
    private String mPrice;
    private PurchaseAddEditItemUsersUser[] mUsers;

    public PurchaseAddEditItem(@NonNull PurchaseAddEditItemUsersUser[] users) {
        mUsers = users;
    }

    public PurchaseAddEditItem(@NonNull String name, @NonNull String price, @NonNull PurchaseAddEditItemUsersUser[] users) {
        mName = name;
        mPrice = price;
        mUsers = users;
    }

    private PurchaseAddEditItem(Parcel in) {
        mValidate = in.readByte() != 0;
        mName = in.readString();
        mPrice = in.readString();
        mUsers = in.createTypedArray(PurchaseAddEditItemUsersUser.CREATOR);
    }

    public void setPriceChangedListener(@NonNull PriceChangedListener priceChangedListener) {
        mPriceChangedListener = priceChangedListener;
    }

    public void setMoneyFormatter(@NonNull NumberFormat moneyFormatter) {
        mMoneyFormatter = moneyFormatter;
    }

    public void updatePriceFormat(@NonNull NumberFormat moneyFormatter) {
        setMoneyFormatter(moneyFormatter);
        setPrice(mMoneyFormatter.format(parsePrice()));
    }

    @Bindable
    public String getName() {
        return mName;
    }

    public void setName(@NonNull String name) {
        mName = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getPrice() {
        return mPrice;
    }

    public void setPrice(@NonNull String price) {
        mPrice = price;
        notifyPropertyChanged(BR.price);
    }

    /**
     * Returns the price parsed as a double. Accounts for localized string formatting.
     *
     * @return the price parsed as a double.
     */
    public double parsePrice() {
        try {
            return Double.parseDouble(mPrice);
        } catch (NumberFormatException e) {
            try {
                return mMoneyFormatter.parse(mPrice).doubleValue();
            } catch (ParseException e1) {
                return 0;
            }
        }
    }

    public PurchaseAddEditItemUsersUser[] getUsers() {
        return mUsers;
    }

    public void setUsers(@NonNull PurchaseAddEditItemUsersUser[] users) {
        mUsers = users;
    }

    @Bindable
    public boolean isValidate() {
        return mValidate;
    }

    @Bindable
    public boolean isNameComplete() {
        return !TextUtils.isEmpty(mName);
    }

    @Bindable
    public boolean isPriceComplete() {
        return !TextUtils.isEmpty(mPrice);
    }

    public void onNameChanged(CharSequence s, int start, int before, int count) {
        mName = s.toString();

        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    public void onPriceChanged(CharSequence s, int start, int before, int count) {
        mPrice = s.toString();
        mPriceChangedListener.onRowPriceChanged();

        if (mValidate) {
            notifyPropertyChanged(BR.validate);
        }
    }

    public void onPriceFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            setPrice(mMoneyFormatter.format(parsePrice()));
        }
    }

    public boolean validateFields() {
        mValidate = true;
        notifyPropertyChanged(BR.validate);
        return isNameComplete() && isPriceComplete();
    }

    public List<String> getSelectedIdentitiesIds() {
        final List<String> userIds = new ArrayList<>();
        for (PurchaseAddEditItemUsersUser user : mUsers) {
            if (user.isSelected()) {
                userIds.add(user.getObjectId());
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
        dest.writeByte(mValidate ? (byte) 1 : (byte) 0);
        dest.writeString(mName);
        dest.writeString(mPrice);
        dest.writeTypedArray(mUsers, 0);
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
