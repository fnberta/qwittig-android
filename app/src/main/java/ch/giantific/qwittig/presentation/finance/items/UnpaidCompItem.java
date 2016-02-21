/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.presentation.common.viewmodels.CardTopProgressViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Created by fabio on 11.02.16.
 */
public abstract class UnpaidCompItem extends BaseObservable
        implements UnpaidItem, CardTopProgressViewModel {

    final Compensation mCompensation;
    boolean mCredit;
    String mCompUsername;
    String mCompUserAvatar;
    final String mCompAmount;
    final boolean mCompLoading;

    public UnpaidCompItem(@NonNull Compensation compensation, @NonNull NumberFormat moneyFormatter) {
        mCompensation = compensation;
        mCompAmount = moneyFormatter.format(compensation.getAmountFraction());
        mCompLoading = compensation.isLoading();
    }

    public Compensation getCompensation() {
        return mCompensation;
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
