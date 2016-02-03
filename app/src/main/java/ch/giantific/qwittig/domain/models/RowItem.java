/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.domain.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
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
public class RowItem extends BaseObservable implements Parcelable {

    public static final Creator<RowItem> CREATOR = new Creator<RowItem>() {
        public RowItem createFromParcel(Parcel source) {
            return new RowItem(source);
        }

        public RowItem[] newArray(int size) {
            return new RowItem[size];
        }
    };
    private PriceChangedListener mPriceChangedListener;
    private String mCurrency;
    private boolean mValidate;
    private String mName;
    private String mPrice;
    private RowItemUser[] mUsers;

    public RowItem(@NonNull RowItemUser[] users, @NonNull String currency) {
        mUsers = users;
        mCurrency = currency;
    }

    public RowItem(@NonNull String name, @NonNull String price, @NonNull RowItemUser[] users,
                   @NonNull String currency) {
        mName = name;
        mPrice = price;
        mUsers = users;
        mCurrency = currency;
    }

    protected RowItem(Parcel in) {
        mValidate = in.readByte() != 0;
        mName = in.readString();
        mPrice = in.readString();
        mUsers = (RowItemUser[]) in.readParcelableArray(RowItemUser.class.getClassLoader());
    }

    public void setPriceChangedListener(@NonNull PriceChangedListener priceChangedListener) {
        mPriceChangedListener = priceChangedListener;
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

    public RowItemUser[] getUsers() {
        return mUsers;
    }

    public void setUsers(@NonNull RowItemUser[] users) {
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
        for (RowItemUser user : mUsers) {
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
