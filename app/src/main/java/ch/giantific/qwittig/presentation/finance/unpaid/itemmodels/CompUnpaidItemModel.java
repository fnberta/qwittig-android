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
public class CompUnpaidItemModel extends BaseChildItemModel
        implements Comparable<CompUnpaidItemModel> {

    private final String amount;
    private final BigFraction amountFraction;
    private final String nickname;
    private final String avatar;
    private final boolean credit;
    private final boolean pending;

    public CompUnpaidItemModel(@EventType int eventType,
                               @NonNull Compensation compensation,
                               @NonNull Identity identity,
                               @NonNull NumberFormat moneyFormatter,
                               boolean isCredit) {
        super(eventType, compensation.getId());

        amountFraction = compensation.getAmountFraction();
        amount = moneyFormatter.format(amountFraction);
        nickname = identity.getNickname();
        avatar = identity.getAvatar();
        credit = isCredit;
        pending = identity.isPending();
    }


    @Bindable
    public String getAmount() {
        return amount;
    }

    public BigFraction getAmountFraction() {
        return amountFraction;
    }

    @Bindable
    public String getNickname() {
        return nickname;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    @Bindable
    public boolean isCredit() {
        return credit;
    }

    public boolean isPending() {
        return pending;
    }

    @Override
    @ViewType
    public int getViewType() {
        return isCredit() ? ViewType.CREDIT : ViewType.DEBT;
    }

    @Override
    public int compareTo(@NonNull CompUnpaidItemModel itemModel) {
        if (credit && !itemModel.isCredit()) {
            return 1;
        }

        return amountFraction.compareTo(itemModel.getAmountFraction());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CompUnpaidItemModel that = (CompUnpaidItemModel) o;

        if (credit != that.isCredit()) return false;
        if (pending != that.isPending()) return false;
        if (!amount.equals(that.getAmount())) return false;
        if (!amountFraction.equals(that.getAmountFraction())) return false;
        if (!nickname.equals(that.getNickname())) return false;
        return avatar != null ? avatar.equals(that.getAvatar()) : that.getAvatar() == null;
    }

    @Override
    public int hashCode() {
        int result = amount.hashCode();
        result = 31 * result + amountFraction.hashCode();
        result = 31 * result + nickname.hashCode();
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (credit ? 1 : 0);
        result = 31 * result + (pending ? 1 : 0);
        return result;
    }

    @IntDef({ViewType.CREDIT, ViewType.DEBT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewType {
        int CREDIT = 1;
        int DEBT = 2;
    }
}
