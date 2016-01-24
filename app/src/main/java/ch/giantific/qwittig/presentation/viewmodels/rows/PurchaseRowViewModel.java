/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 21.01.16.
 */
public class PurchaseRowViewModel extends BaseObservable {

    private User mCurrentUser;
    private boolean mPurchaseBuyerValid;
    private String mPurchaseBuyerAndDate;
    private byte[] mPurchaseBuyerAvatar;
    private String mPurchaseStore;
    private String mPurchaseTotalPrice;
    private String mPurchaseMyShare;
    private boolean mPurchaseRead;

    public PurchaseRowViewModel(@NonNull Purchase purchase, @NonNull User currentUser) {
        mCurrentUser = currentUser;
        setPurchaseInfo(purchase);
    }

    private void setPurchaseInfo(@NonNull Purchase purchase) {
        final User buyer = purchase.getBuyer();
        final Group currentGroup = mCurrentUser.getCurrentGroup();
        mPurchaseBuyerValid = buyer.getGroupIds().contains(currentGroup.getObjectId());
        // TODO: show me if buyer == currentUser and check for if deleted
        mPurchaseBuyerAndDate = String.format("%s, %s", buyer.getNickname(),
                DateUtils.formatDateShort(purchase.getDate()));
        mPurchaseBuyerAvatar = buyer.getAvatar();
        mPurchaseStore = purchase.getStore();
        final String currency = currentGroup.getCurrency();
        mPurchaseTotalPrice = MoneyUtils.formatMoneyNoSymbol(purchase.getTotalPrice(), currency);
        mPurchaseMyShare = MoneyUtils.formatMoneyNoSymbol(purchase.calculateUserShare(mCurrentUser), currency);
        // TODO: implement ripple for <21 (just show white color)
        mPurchaseRead = purchase.userHasReadPurchase(mCurrentUser);
    }

    public void updatePurchaseInfo(@NonNull Purchase purchase) {
        setPurchaseInfo(purchase);
        notifyChange();
    }

    @Bindable
    public boolean isPurchaseBuyerValid() {
        return mPurchaseBuyerValid;
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
