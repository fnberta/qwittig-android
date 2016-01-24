/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 19.01.16.
 */
public class CompPaidRowViewModel extends BaseObservable {

    private final User mCurrentUser;
    private final Group mCurrentGroup;
    private boolean mCompUserValid;
    private String mCompUsername;
    private byte[] mCompUserAvatar;
    private String mCompAmount;
    private boolean mCompAmountPos;
    private String mCompDate;

    public CompPaidRowViewModel(@NonNull Compensation compensation,
                                @NonNull User currentUser) {

        mCurrentGroup = currentUser.getCurrentGroup();
        mCurrentUser = currentUser;
        setCompInfo(compensation);
        mCompDate = DateUtils.formatDateShort(compensation.getCreatedAt());
    }

    private void setCompInfo(@NonNull Compensation compensation) {
        final User beneficiary = compensation.getBeneficiary();
        final BigFraction amount = compensation.getAmountFraction();
        final String currency = mCurrentGroup.getCurrency();
        final String groupId = mCurrentGroup.getObjectId();
        if (beneficiary.getObjectId().equals(mCurrentUser.getObjectId())) {
            final User payer = compensation.getPayer();
            mCompUserValid = payer.getGroupIds().contains(groupId);
            mCompUsername = payer.getNickname();
            mCompUserAvatar = payer.getAvatar();
            mCompAmount = MoneyUtils.formatMoney(amount, currency);
            mCompAmountPos = true;
        } else {
            mCompUserValid = beneficiary.getGroupIds().contains(groupId);
            mCompUsername = beneficiary.getNickname();
            mCompUserAvatar = beneficiary.getAvatar();
            mCompAmountPos = false;
            mCompAmount = MoneyUtils.formatMoney(amount.negate(), currency);
        }
    }

    public void updateCompInfo(@NonNull Compensation compensation) {
        setCompInfo(compensation);
        notifyChange();
    }

    @Bindable
    public boolean isCompUserValid() {
        return mCompUserValid;
    }

    @Bindable
    public String getCompUsername() {
        return mCompUsername;
    }

    @Bindable
    public byte[] getCompUserAvatar() {
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
