/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.itemmodels.BaseChildItemModel;
import ch.giantific.qwittig.utils.DateUtils;

/**
 * Provides a view model for a paid compensation list row.
 */
public class CompsPaidItemModel extends BaseChildItemModel
        implements Comparable<CompsPaidItemModel> {

    private final Date mDate;
    private final String mAmount;
    private final boolean mAmountPos;
    private final String mPaidAt;
    private final String mNickname;
    private final String mAvatar;

    public CompsPaidItemModel(@EventType int eventType,
                              @NonNull Compensation compensation,
                              @NonNull Identity identity,
                              boolean isPos,
                              @NonNull NumberFormat numberFormat) {
        super(eventType, identity.getId());

        mDate = compensation.getPaidAtDate();
        mAmount = numberFormat.format(compensation.getAmountFraction());
        mAmountPos = isPos;
        final DateFormat dateFormatter = DateUtils.getDateFormatter(true);
        mPaidAt = dateFormatter.format(mDate);
        mNickname = identity.getNickname();
        mAvatar = identity.getAvatar();
    }

    public Date getDate() {
        return mDate;
    }

    @Bindable
    public String getAmount() {
        return mAmount;
    }

    @Bindable
    public boolean isAmountPos() {
        return mAmountPos;
    }

    @Bindable
    public String getPaidAt() {
        return mPaidAt;
    }

    @Bindable
    public String getNickname() {
        return mNickname;
    }

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    @Override
    public int compareTo(@NonNull CompsPaidItemModel compsPaidItemModel) {
        return mDate.compareTo(compsPaidItemModel.getDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CompsPaidItemModel that = (CompsPaidItemModel) o;

        if (mAmountPos != that.isAmountPos()) return false;
        if (!mDate.equals(that.getDate())) return false;
        if (!mAmount.equals(that.getAmount())) return false;
        if (!mNickname.equals(that.getNickname())) return false;
        return mAvatar != null ? mAvatar.equals(that.getAvatar()) : that.getAvatar() == null;

    }

    @Override
    public int hashCode() {
        int result = mDate.hashCode();
        result = 31 * result + mAmount.hashCode();
        result = 31 * result + (mAmountPos ? 1 : 0);
        result = 31 * result + mNickname.hashCode();
        result = 31 * result + (mAvatar != null ? mAvatar.hashCode() : 0);
        return result;
    }
}
