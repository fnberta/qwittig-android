/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

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

    private final Date date;
    private final String amount;
    private final boolean amountPos;
    private final String paidAt;
    private final String nickname;
    private final String avatar;

    public CompsPaidItemModel(@EventType int eventType,
                              @NonNull Compensation compensation,
                              @NonNull Identity identity,
                              boolean isPos,
                              @NonNull NumberFormat numberFormat) {
        super(eventType, identity.getId());

        date = compensation.getPaidAtDate();
        amount = numberFormat.format(compensation.getAmountFraction());
        amountPos = isPos;
        final DateFormat dateFormatter = DateUtils.getDateFormatter(true);
        paidAt = dateFormatter.format(date);
        nickname = identity.getNickname();
        avatar = identity.getAvatar();
    }

    public Date getDate() {
        return date;
    }

    @Bindable
    public String getAmount() {
        return amount;
    }

    @Bindable
    public boolean isAmountPos() {
        return amountPos;
    }

    @Bindable
    public String getPaidAt() {
        return paidAt;
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    @Override
    public int compareTo(@NonNull CompsPaidItemModel compsPaidItemModel) {
        return date.compareTo(compsPaidItemModel.getDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CompsPaidItemModel that = (CompsPaidItemModel) o;

        if (amountPos != that.isAmountPos()) return false;
        if (!date.equals(that.getDate())) return false;
        if (!amount.equals(that.getAmount())) return false;
        if (!nickname.equals(that.getNickname())) return false;
        return avatar != null ? avatar.equals(that.getAvatar()) : that.getAvatar() == null;

    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + (amountPos ? 1 : 0);
        result = 31 * result + nickname.hashCode();
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        return result;
    }
}
