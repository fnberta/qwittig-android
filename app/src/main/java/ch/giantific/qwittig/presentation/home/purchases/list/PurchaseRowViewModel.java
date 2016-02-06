/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 21.01.16.
 */
public class PurchaseRowViewModel extends BaseObservable {

    private Identity mCurrentIdentity;
    private String mPurchaseBuyerAndDate;
    private byte[] mPurchaseBuyerAvatar;
    private String mPurchaseStore;
    private String mPurchaseTotalPrice;
    private String mPurchaseMyShare;
    private boolean mPurchaseRead;
    private String mCurrency;

    public PurchaseRowViewModel(@NonNull Purchase purchase, @NonNull Identity currentIdentity) {
        mCurrentIdentity = currentIdentity;
        mCurrency = currentIdentity.getGroup().getCurrency();
        setPurchaseInfo(purchase);
    }

    private void setPurchaseInfo(@NonNull Purchase purchase) {
        final Identity buyer = purchase.getBuyer();
        // TODO: show me if buyer == currentUser
        mPurchaseBuyerAndDate = String.format("%s, %s", buyer.getNickname(),
                DateUtils.formatDateShort(purchase.getDate()));
        mPurchaseBuyerAvatar = buyer.getAvatar();
        mPurchaseStore = purchase.getStore();
        mPurchaseTotalPrice = MoneyUtils.formatMoneyNoSymbol(purchase.getTotalPrice(), mCurrency);
        mPurchaseMyShare = MoneyUtils.formatMoneyNoSymbol(purchase.calculateUserShare(mCurrentIdentity), mCurrency);
        // TODO: implement ripple for <21 (just show white color)
        mPurchaseRead = purchase.userHasReadPurchase(mCurrentIdentity);
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
    public byte[] getPurchaseBuyerAvatar() {
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
