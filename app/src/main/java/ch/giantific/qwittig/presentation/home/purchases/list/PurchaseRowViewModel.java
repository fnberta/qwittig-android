/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 21.01.16.
 */
public class PurchaseRowViewModel extends BaseObservable {

    private final Identity mCurrentIdentity;
    private final NumberFormat mMoneyFormatter;
    private final DateFormat mDateFormatter;
    private String mPurchaseBuyerAndDate;
    private String mPurchaseBuyerAvatar;
    private String mPurchaseStore;
    private String mPurchaseTotalPrice;
    private String mPurchaseMyShare;
    private boolean mPurchaseRead;

    public PurchaseRowViewModel(@NonNull Purchase purchase, @NonNull Identity currentIdentity) {
        mCurrentIdentity = currentIdentity;
        final String currency = currentIdentity.getGroup().getCurrency();
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);
        mDateFormatter = DateUtils.getDateFormatter(true);
        setPurchaseInfo(purchase);
    }

    private void setPurchaseInfo(@NonNull Purchase purchase) {
        final Identity buyer = purchase.getBuyer();
        // TODO: show me if buyer == currentUser
        mPurchaseBuyerAndDate = String.format("%s, %s", buyer.getNickname(),
                mDateFormatter.format(purchase.getDate()));
        mPurchaseBuyerAvatar = buyer.getAvatarUrl();
        mPurchaseStore = purchase.getStore();
        mPurchaseTotalPrice = mMoneyFormatter.format(purchase.getTotalPrice());
        mPurchaseMyShare = mMoneyFormatter.format(purchase.calculateUserShare(mCurrentIdentity));
        // TODO: implement ripple for <21 (just show white color)
        mPurchaseRead = purchase.isRead(mCurrentIdentity);
    }

    public void updatePurchaseInfo(@NonNull Purchase purchase) {
        setPurchaseInfo(purchase);
        notifyChange();
    }

    @Bindable
    public String getPurchaseBuyerAndDate() {
        return mPurchaseBuyerAndDate;
    }

    @Bindable
    public String getPurchaseBuyerAvatar() {
        return mPurchaseBuyerAvatar;
    }

    @Bindable
    public String getPurchaseStore() {
        return mPurchaseStore;
    }

    @Bindable
    public String getPurchaseTotalPrice() {
        return mPurchaseTotalPrice;
    }

    @Bindable
    public String getPurchaseMyShare() {
        return mPurchaseMyShare;
    }

    @Bindable
    public boolean isPurchaseRead() {
        return mPurchaseRead;
    }
}
