/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 19.01.16.
 */
public class CompPaidRowViewModel extends BaseObservable {

    private final Identity mCurrentIdentity;
    private final String mCurrency;
    private String mCompUsername;
    private String mCompUserAvatar;
    private String mCompAmount;
    private boolean mCompAmountPos;
    private String mCompDate;

    public CompPaidRowViewModel(@NonNull Compensation compensation,
                                @NonNull Identity currentIdentity) {

        mCurrency = currentIdentity.getGroup().getCurrency();
        mCurrentIdentity = currentIdentity;
        setCompInfo(compensation);
        mCompDate = DateUtils.formatDateShort(compensation.getCreatedAt());
    }

    private void setCompInfo(@NonNull Compensation compensation) {
        final Identity creditor = compensation.getCreditor();
        final BigFraction amount = compensation.getAmountFraction();
        if (creditor.getObjectId().equals(mCurrentIdentity.getObjectId())) {
            final Identity debtor = compensation.getDebtor();
            mCompUsername = debtor.getNickname();
            mCompUserAvatar = debtor.getAvatarUrl();
            mCompAmount = MoneyUtils.formatMoney(amount, mCurrency);
            mCompAmountPos = true;
        } else {
            mCompUsername = creditor.getNickname();
            mCompUserAvatar = creditor.getAvatarUrl();
            mCompAmountPos = false;
            mCompAmount = MoneyUtils.formatMoney(amount.negate(), mCurrency);
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
