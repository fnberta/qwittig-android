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

/**
 * Created by fabio on 11.02.16.
 */
public abstract class UnpaidCompItem extends BaseObservable
        implements UnpaidItem, CardTopProgressViewModel {

    private final Compensation mCompensation;
    private final String mCompAmount;
    private final boolean mCompLoading;
    boolean mCredit;
    String mCompUsername;
    String mCompUserAvatar;

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
