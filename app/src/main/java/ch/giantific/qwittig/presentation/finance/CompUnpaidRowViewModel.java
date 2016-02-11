/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 19.01.16.
 */
public class CompUnpaidRowViewModel extends BaseObservable implements CardTopProgressViewModel {

    private final String mCurrency;
    private boolean mCredit;
    private String mCompUsername;
    private String mCompUserAvatar;
    private String mCompAmount;
    private boolean mCompLoading;

    public CompUnpaidRowViewModel(@NonNull Compensation compensation,
                                  @NonNull String currency, boolean credit) {
        mCurrency = currency;
        setCompInfo(compensation, credit);
    }

    private void setCompInfo(@NonNull Compensation compensation, boolean credit) {
        mCredit = credit;
        final Identity identity = credit ? compensation.getDebtor() : compensation.getCreditor();
        mCompUsername = identity.getNickname();
        mCompUserAvatar = identity.getAvatarUrl();
        mCompAmount = MoneyUtils.formatMoney(compensation.getAmountFraction(), mCurrency);
        mCompLoading = compensation.isLoading();
    }

    public void updateCompInfo(@NonNull Compensation compensation, boolean credit) {
        setCompInfo(compensation, credit);
        notifyChange();
    }

    @Bindable
    public boolean isCredit() {
        return mCredit;
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

    @Override
    @Bindable
    public boolean isItemLoading() {
        return mCompLoading;
    }
}
