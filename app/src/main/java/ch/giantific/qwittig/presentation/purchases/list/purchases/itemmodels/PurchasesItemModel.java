/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases.itemmodels;

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
 * Provides a view model for a purchase in the list of purchases screen.
 */
public class PurchasesItemModel extends BaseObservable {

    private final Identity mCurrentIdentity;
    private final NumberFormat mMoneyFormatter;
    private final DateFormat mDateFormatter;
    private Purchase mPurchase;

    public PurchasesItemModel(@NonNull Purchase purchase, @NonNull Identity currentIdentity) {
        mCurrentIdentity = currentIdentity;
        final String currency = currentIdentity.getGroup().getCurrency();
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);
        mDateFormatter = DateUtils.getDateFormatter(true);
        mPurchase = purchase;
    }

    public void updatePurchaseInfo(@NonNull Purchase purchase) {
        mPurchase = purchase;
        notifyChange();
    }

    @Bindable
    public Purchase getPurchase() {
        return mPurchase;
    }

    @Bindable
    public String getPurchaseBuyerAndDate() {
        // TODO: show me if buyer == currentUser
        return String.format("%s, %s", mPurchase.getBuyer().getNickname(),
                mDateFormatter.format(mPurchase.getDate()));
    }

    @Bindable
    public String getPurchaseBuyerAvatar() {
        return mPurchase.getBuyer().getAvatarUrl();
    }

    @Bindable
    public String getPurchaseStore() {
        return mPurchase.getStore();
    }

    @Bindable
    public String getPurchaseTotalPrice() {
        return mMoneyFormatter.format(mPurchase.getTotalPrice());
    }

    @Bindable
    public String getPurchaseMyShare() {
        return mMoneyFormatter.format(mPurchase.calculateUserShare(mCurrentIdentity));
    }

    @Bindable
    public boolean isPurchaseRead() {
        // TODO: implement ripple for <21 (just show white color)
        return mPurchase.isRead(mCurrentIdentity);
    }
}
