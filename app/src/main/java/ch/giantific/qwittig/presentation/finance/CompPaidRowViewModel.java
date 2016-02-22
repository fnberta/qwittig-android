/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.DateFormat;
import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 19.01.16.
 */
public class CompPaidRowViewModel extends BaseObservable {

    private final Identity mCurrentIdentity;
    private final NumberFormat mMoneyFormatter;
    private final DateFormat mDateFormatter;
    private String mCompDate;
    private String mCompUsername;
    private String mCompUserAvatar;
    private String mCompAmount;
    private boolean mCompAmountPos;

    public CompPaidRowViewModel(@NonNull Compensation compensation,
                                @NonNull Identity currentIdentity) {

        final String currency = currentIdentity.getGroup().getCurrency();
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, true, true);
        mDateFormatter = DateUtils.getDateFormatter(true);
        mCurrentIdentity = currentIdentity;
        setCompInfo(compensation);
    }

    private void setCompInfo(@NonNull Compensation compensation) {
        mCompDate = mDateFormatter.format(compensation.getCreatedAt());
        final Identity creditor = compensation.getCreditor();
        final BigFraction amount = compensation.getAmountFraction();
        if (creditor.getObjectId().equals(mCurrentIdentity.getObjectId())) {
            final Identity debtor = compensation.getDebtor();
            mCompUsername = debtor.getNickname();
            mCompUserAvatar = debtor.getAvatarUrl();
            mCompAmount = mMoneyFormatter.format(amount);
            mCompAmountPos = true;
        } else {
            mCompUsername = creditor.getNickname();
            mCompUserAvatar = creditor.getAvatarUrl();
            mCompAmountPos = false;
            mCompAmount = mMoneyFormatter.format(amount.negate());
        }
    }

    public void updateCompInfo(@NonNull Compensation compensation) {
        setCompInfo(compensation);
        notifyChange();
    }

    @Bindable
    public String getCompUsername() {
        return mCompUsername;
    }

    @Bindable
    public String getCompUserAvatar() {
        return mCompUserAvatar;
    }

    @Bindable
    public String getCompAmount() {
        return mCompAmount;
    }

    @Bindable
    public boolean isCompAmountPos() {
        return mCompAmountPos;
    }

    @Bindable
    public String getCompDate() {
        return mCompDate;
    }
}
