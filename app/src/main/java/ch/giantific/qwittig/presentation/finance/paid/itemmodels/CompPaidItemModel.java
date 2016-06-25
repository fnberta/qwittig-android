/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Objects;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Provides a view model for a paid compensation list row.
 */
public class CompPaidItemModel extends BaseObservable {

    private final NumberFormat mMoneyFormatter;
    private final DateFormat mDateFormatter;
    private Compensation mCompensation;
    private boolean mCompAmountPos;

    public CompPaidItemModel(@NonNull Compensation compensation,
                             @NonNull Identity currentIdentity) {
        mCompensation = compensation;
        mCompAmountPos = Objects.equals(compensation.getCreditor().getObjectId(), currentIdentity.getObjectId());

        final String currency = currentIdentity.getGroup().getCurrency();
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, true, true);
        mDateFormatter = DateUtils.getDateFormatter(true);
    }

    public void updateCompensation(@NonNull Compensation compensation) {
        mCompensation = compensation;
        notifyChange();
    }

    @Bindable
    public String getCompUsername() {
        return mCompAmountPos ? mCompensation.getDebtor().getNickname() : mCompensation.getCreditor().getNickname();
    }

    @Bindable
    public String getCompUserAvatar() {
        return mCompAmountPos ? mCompensation.getDebtor().getAvatarUrl() : mCompensation.getCreditor().getAvatarUrl();
    }

    @Bindable
    public String getCompAmount() {
        final BigFraction amount = mCompensation.getAmountFraction();
        return mMoneyFormatter.format(mCompAmountPos ? amount : amount.negate());
    }

    @Bindable
    public boolean isCompAmountPos() {
        return mCompAmountPos;
    }

    @Bindable
    public String getCompDate() {
        return mDateFormatter.format(mCompensation.getCreatedAt());
    }
}
