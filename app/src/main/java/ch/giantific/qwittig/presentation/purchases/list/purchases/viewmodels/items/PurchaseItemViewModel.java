/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.items;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.viewmodels.items.BaseChildItemViewModel;

/**
 * Provides a view model for a purchase in the list of purchases screen.
 */
public class PurchaseItemViewModel extends BaseChildItemViewModel
        implements Comparable<PurchaseItemViewModel> {

    private final Date date;
    private final String buyerNicknameAndDate;
    private final String buyerAvatar;
    private final String store;
    private final String total;
    private final String userShare;
    private final boolean read;

    public PurchaseItemViewModel(@EventType int eventType,
                                 @NonNull Purchase purchase,
                                 @NonNull Identity buyer,
                                 @NonNull String currentIdentityId,
                                 @NonNull NumberFormat moneyFormatter,
                                 @NonNull DateFormat dateFormatter) {
        super(eventType, purchase.getId());

        date = purchase.getDateDate();
        buyerNicknameAndDate = String.format("%s, %s", buyer.getNickname(), dateFormatter.format(date));
        buyerAvatar = buyer.getAvatar();
        store = purchase.getStore();
        total = moneyFormatter.format(purchase.getTotal());
        userShare = moneyFormatter.format(purchase.calculateUserShare(currentIdentityId));
        read = purchase.isRead(currentIdentityId);
    }

    public Date getDate() {
        return date;
    }

    @Bindable
    public String getBuyerNicknameAndDate() {
        return buyerNicknameAndDate;
    }

    @Bindable
    public String getBuyerAvatar() {
        return buyerAvatar;
    }

    @Bindable
    public String getStore() {
        return store;
    }

    @Bindable
    public String getTotal() {
        return total;
    }

    @Bindable
    public String getUserShare() {
        return userShare;
    }

    @Bindable
    public boolean isRead() {
        return read;
    }

    @Override
    public int compareTo(@NonNull PurchaseItemViewModel itemViewModel) {
        return itemViewModel.getDate().compareTo(date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PurchaseItemViewModel that = (PurchaseItemViewModel) o;

        if (read != that.isRead()) return false;
        if (!buyerNicknameAndDate.equals(that.getBuyerNicknameAndDate())) return false;
        if (buyerAvatar != null ? !buyerAvatar.equals(that.getBuyerAvatar()) : that.getBuyerAvatar() != null)
            return false;
        if (!store.equals(that.getStore())) return false;
        if (!total.equals(that.getTotal())) return false;
        return userShare.equals(that.getUserShare());

    }

    @Override
    public int hashCode() {
        int result = buyerNicknameAndDate.hashCode();
        result = 31 * result + (buyerAvatar != null ? buyerAvatar.hashCode() : 0);
        result = 31 * result + store.hashCode();
        result = 31 * result + total.hashCode();
        result = 31 * result + userShare.hashCode();
        result = 31 * result + (read ? 1 : 0);
        return result;
    }
}
