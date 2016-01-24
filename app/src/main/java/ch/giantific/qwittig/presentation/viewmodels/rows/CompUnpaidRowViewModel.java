/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 19.01.16.
 */
public class CompUnpaidRowViewModel extends BaseObservable implements CardTopProgressViewModel {

    private final String mCurrency;
    private String mCompUsername;
    private byte[] mCompUserAvatar;
    private String mCompAmount;
    private boolean mCompLoading;

    public CompUnpaidRowViewModel(@NonNull Compensation compensation,
                                  @NonNull String currency) {
        mCurrency = currency;
        setCompInfo(compensation);
    }

    private void setCompInfo(@NonNull Compensation compensation) {
        final User payer = compensation.getPayer();
        mCompUsername = payer.getNickname();
        mCompUserAvatar = payer.getAvatar();
        mCompAmount = MoneyUtils.formatMoney(compensation.getAmountFraction(), mCurrency);
        mCompLoading = compensation.isLoading();
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
    public byte[] getCompUserAvatar() {
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
