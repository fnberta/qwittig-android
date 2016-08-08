/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseChildItemModel;

/**
 * Provides a list compensation item.
 */
public class CompsUnpaidItemModel extends BaseChildItemModel
        implements Comparable<CompsUnpaidItemModel> {

    private final String mAmount;
    private final BigFraction mAmountFraction;
    private final String mNickname;
    private final String mAvatar;
    private final boolean mCredit;
    private final boolean mIdentityPending;

    public CompsUnpaidItemModel(@EventType int eventType,
                                @NonNull Compensation compensation,
                                @NonNull Identity identity,
                                @NonNull NumberFormat moneyFormatter,
                                boolean isCredit) {
        super(eventType, compensation.getId());

        mAmountFraction = compensation.getAmountFraction();
        mAmount = moneyFormatter.format(mAmountFraction);
        mNickname = identity.getNickname();
        mAvatar = identity.getAvatar();
        mCredit = isCredit;
        mIdentityPending = identity.isPending();
    }


    @Bindable
    public String getAmount() {
        return mAmount;
    }

    public BigFraction getAmountFraction() {
        return mAmountFraction;
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    @Bindable
    public boolean isCredit() {
        return mCredit;
    }

    @Bindable
    public boolean isIdentityPending() {
        return mIdentityPending;
    }

    @Override
    @ViewType
    public int getViewType() {
        return isCredit() ? ViewType.CREDIT : ViewType.DEBT;
    }

    @Override
    public int compareTo(@NonNull CompsUnpaidItemModel itemModel) {
        if (mCredit && !itemModel.isCredit()) {
            return 1;
        }

        return mAmountFraction.compareTo(itemModel.getAmountFraction());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CompsUnpaidItemModel that = (CompsUnpaidItemModel) o;

        if (mCredit != that.isCredit()) return false;
        if (mIdentityPending != that.isIdentityPending()) return false;
        if (!mAmount.equals(that.getAmount())) return false;
        if (!mAmountFraction.equals(that.getAmountFraction())) return false;
        if (!mNickname.equals(that.getNickname())) return false;
        return mAvatar != null ? mAvatar.equals(that.getAvatar()) : that.getAvatar() == null;

    }

    @Override
    public int hashCode() {
        int result = mAmount.hashCode();
        result = 31 * result + mAmountFraction.hashCode();
        result = 31 * result + mNickname.hashCode();
        result = 31 * result + (mAvatar != null ? mAvatar.hashCode() : 0);
        result = 31 * result + (mCredit ? 1 : 0);
        result = 31 * result + (mIdentityPending ? 1 : 0);
        return result;
    }

    @IntDef({ViewType.CREDIT, ViewType.DEBT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewType {
        int CREDIT = 1;
        int DEBT = 2;
    }
}
