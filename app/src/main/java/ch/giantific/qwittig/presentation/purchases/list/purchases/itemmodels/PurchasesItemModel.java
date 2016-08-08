/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseChildItemModel;

/**
 * Provides a view model for a purchase in the list of purchases screen.
 */
public class PurchasesItemModel extends BaseChildItemModel
        implements Comparable<PurchasesItemModel> {

    private final Date mDate;
    private final String mBuyerNicknameAndDate;
    private final String mBuyerAvatar;
    private final String mStore;
    private final String mTotal;
    private final String mUserShare;
    private final boolean mRead;

    public PurchasesItemModel(@EventType int eventType,
                              @NonNull Purchase purchase,
                              @NonNull Identity buyer,
                              @NonNull String currentIdentityId,
                              @NonNull NumberFormat moneyFormatter,
                              @NonNull DateFormat dateFormatter) {
        super(eventType, purchase.getId());

        mDate = purchase.getDateDate();
        mBuyerNicknameAndDate = String.format("%s, %s", buyer.getNickname(), dateFormatter.format(mDate));
        mBuyerAvatar = buyer.getAvatar();
        mStore = purchase.getStore();
        mTotal = moneyFormatter.format(purchase.getTotal());
        mUserShare = moneyFormatter.format(purchase.calculateUserShare(currentIdentityId));
        mRead = purchase.isRead(currentIdentityId);
    }

    public Date getDate() {
        return mDate;
    }

    @Bindable
    public String getBuyerNicknameAndDate() {
        return mBuyerNicknameAndDate;
    }

    @Bindable
    public String getBuyerAvatar() {
        return mBuyerAvatar;
    }

    @Bindable
    public String getStore() {
        return mStore;
    }

    @Bindable
    public String getTotal() {
        return mTotal;
    }

    @Bindable
    public String getUserShare() {
        return mUserShare;
    }

    @Bindable
    public boolean isRead() {
        return mRead;
    }

    @Override
    public int compareTo(@NonNull PurchasesItemModel itemModel) {
        return itemModel.getDate().compareTo(mDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PurchasesItemModel that = (PurchasesItemModel) o;

        if (mRead != that.isRead()) return false;
        if (!mBuyerNicknameAndDate.equals(that.getBuyerNicknameAndDate())) return false;
        if (mBuyerAvatar != null ? !mBuyerAvatar.equals(that.getBuyerAvatar()) : that.getBuyerAvatar() != null)
            return false;
        if (!mStore.equals(that.getStore())) return false;
        if (!mTotal.equals(that.getTotal())) return false;
        return mUserShare.equals(that.getUserShare());

    }

    @Override
    public int hashCode() {
        int result = mBuyerNicknameAndDate.hashCode();
        result = 31 * result + (mBuyerAvatar != null ? mBuyerAvatar.hashCode() : 0);
        result = 31 * result + mStore.hashCode();
        result = 31 * result + mTotal.hashCode();
        result = 31 * result + mUserShare.hashCode();
        result = 31 * result + (mRead ? 1 : 0);
        return result;
    }
}
