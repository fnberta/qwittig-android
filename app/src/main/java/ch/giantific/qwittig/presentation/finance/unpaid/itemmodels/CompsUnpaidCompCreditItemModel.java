/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid.itemmodels;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;

/**
 * Provides an implementation of the {@link CompsUnpaidItemModel} for a unpaid credit item.
 * <p>
 * Subclass of {@link CompsUnpaidCompItemModelBaseImpl}.
 */
public class CompsUnpaidCompCreditItemModel extends CompsUnpaidCompItemModelBaseImpl {

    public CompsUnpaidCompCreditItemModel(@NonNull Compensation compensation,
                                          @NonNull NumberFormat moneyFormatter,
                                          boolean itemLoading) {
        super(compensation, moneyFormatter, itemLoading);
    }

    @Bindable
    @Override
    public boolean isCredit() {
        return true;
    }

    @Override
    protected Identity getIdentity() {
        return mCompensation.getDebtor();
    }

    @Override
    public int getType() {
        return Type.CREDIT;
    }
}
