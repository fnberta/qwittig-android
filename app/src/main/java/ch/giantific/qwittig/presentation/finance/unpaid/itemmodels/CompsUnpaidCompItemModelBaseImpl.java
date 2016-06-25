/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid.itemmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;

/**
 * Provides an abstract base implementation of the {@link CompsUnpaidItemModel} for a compensation
 * item.
 */
public abstract class CompsUnpaidCompItemModelBaseImpl extends BaseObservable
        implements CompsUnpaidCompItemModel {

    final Compensation mCompensation;
    private final NumberFormat mMoneyFormatter;
    private boolean mItemLoading;

    public CompsUnpaidCompItemModelBaseImpl(@NonNull Compensation compensation,
                                            @NonNull NumberFormat moneyFormatter,
                                            boolean itemLoading) {
        mCompensation = compensation;
        mMoneyFormatter = moneyFormatter;
        mItemLoading = itemLoading;
    }

    @Override
    public Compensation getCompensation() {
        return mCompensation;
    }

    @Override
    public String getId() {
        return mCompensation.getObjectId();
    }

    @Override
    @Bindable
    public String getCompAmount() {
        return mMoneyFormatter.format(mCompensation.getAmountFraction());
    }

    @Override
    public BigFraction getCompAmountRaw() {
        return mCompensation.getAmountFraction();
    }

    @Override
    @Bindable
    public String getCompUsername() {
        return getIdentity().getNickname();
    }

    @Override
    @Bindable
    public String getCompUserAvatar() {
        return getIdentity().getAvatarUrl();
    }

    @Override
    @Bindable
    public boolean isUserPending() {
        return getIdentity().isPending();
    }

    protected abstract Identity getIdentity();

    @Override
    @Bindable
    public boolean isItemLoading() {
        return mItemLoading;
    }

    @Override
    public void setItemLoading(boolean itemLoading) {
        mItemLoading = itemLoading;
    }
}
