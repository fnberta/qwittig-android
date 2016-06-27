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
 * Provides an implementation of the {@link CompsUnpaidItemModel} for a unpaid debit item.
 * <p/>
 * Subclass of {@link CompsUnpaidCompItemModelBaseImpl}.
 */
public class CompsUnpaidCompDebtItemModel extends CompsUnpaidCompItemModelBaseImpl {

    public CompsUnpaidCompDebtItemModel(@NonNull Compensation compensation,
                                        @NonNull NumberFormat moneyFormatter,
                                        boolean itemLoading) {
        super(compensation, moneyFormatter, itemLoading);
    }

    @Bindable
    @Override
    public boolean isCredit() {
        return false;
    }

    @Override
    protected Identity getIdentity() {
        return mCompensation.getCreditor();
    }

    @Override
    public int getType() {
        return Type.DEBT;
    }
}
