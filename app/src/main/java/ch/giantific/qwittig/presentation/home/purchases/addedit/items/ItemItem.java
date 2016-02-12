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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 24.01.16.
 */
public class ItemItem extends BaseObservable implements AddEditItem {

    public static final Creator<ItemItem> CREATOR = new Creator<ItemItem>() {
        @Override
        public ItemItem createFromParcel(Parcel source) {
            return new ItemItem(source);
        }

        @Override
        public ItemItem[] newArray(int size) {
            return new ItemItem[size];
        }
    };
    private PriceChangedListener mPriceChangedListener;
    private String mCurrency;
    private boolean mValidate;
    private String mName;
    private String mPrice;
    private ItemUsersItemUser[] mUsers;

    public ItemItem(@NonNull ItemUsersItemUser[] users, @NonNull String currency) {
        mUsers = users;
        mCurrency = currency;
    }

    public ItemItem(@NonNull String name, @NonNull String price, @NonNull ItemUsersItemUser[] users,
                    @NonNull String currency) {
        mName = name;
        mPrice = price;
        mUsers = users;
        mCurrency = currency;
    }

    protected ItemItem(Parcel in) {
        mValidate = in.readByte() != 0;
        mName = in.readString();
        mPrice = in.readString();
        mUsers = (ItemUsersItemUser[]) in.readParcelableArray(ItemUsersItemUser.class.getClassLoader());
    }

    public void setPriceChangedListener(@NonNull PriceChangedListener priceChangedListener) {
        mPriceChangedListener = priceChangedListener;
    }

    @Override
    public int getType() {
        return Type.ITEM;
    }

    public void updateCurrency(@NonNull String currency) {
        mCurrency = currency;
        setPrice(MoneyUtils.formatPrice(mPrice, mCurrency));
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

    public ItemUsersItemUser[] getUsers() {
        return mUsers;
    }

    public void setUsers(@NonNull ItemUsersItemUser[] users) {
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
            setPrice(MoneyUtils.formatPrice(mPrice, mCurrency));
        }
    }

    public boolean validateFields() {
        mValidate = true;
        notifyPropertyChanged(BR.validate);
        return isNameComplete() && isPriceComplete();
    }

    public BigDecimal parsePrice(@NonNull String currency) {
        int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(currency);
        return MoneyUtils.parsePrice(mPrice).setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP);
    }

    public List<String> getSelectedUserIds() {
        final List<String> userIds = new ArrayList<>();
        for (ItemUsersItemUser user : mUsers) {
            if (user.isSelected()) {
                userIds.add(user.getObjectId());
            }
        }
        return userIds;
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
        dest.writeParcelableArray(mUsers, 0);
    }

    public interface PriceChangedListener {
        void onRowPriceChanged();
    }
}
