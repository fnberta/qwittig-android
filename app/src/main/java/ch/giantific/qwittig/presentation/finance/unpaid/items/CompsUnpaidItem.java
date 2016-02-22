/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid.items;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.presentation.common.viewmodels.CardTopProgressViewModel;

/**
 * Provides an abstract base implementation of the {@link CompsUnpaidBaseItem} for a compensation
 * item.
 */
public abstract class CompsUnpaidItem extends BaseObservable
        implements CompsUnpaidBaseItem, CardTopProgressViewModel {

    private final Compensation mCompensation;
    private final String mCompAmount;
    private final boolean mCompLoading;
    boolean mCredit;
    String mCompUsername;
    String mCompUserAvatar;

    public CompsUnpaidItem(@NonNull Compensation compensation, @NonNull NumberFormat moneyFormatter) {
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
