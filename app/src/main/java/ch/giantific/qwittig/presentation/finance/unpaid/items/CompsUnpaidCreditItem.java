/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid.items;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;

/**
 * Provides an implementation of the {@link CompsUnpaidBaseItem} for a unpaid credit item.
 * <p/>
 * Subclass of {@link CompsUnpaidItem}.
 */
public class CompsUnpaidCreditItem extends CompsUnpaidItem {

    private final boolean mUserPending;

    public CompsUnpaidCreditItem(@NonNull Compensation compensation, @NonNull NumberFormat moneyFormatter) {
        super(compensation, moneyFormatter);

        mCredit = true;
        final Identity identity = compensation.getDebtor();
        mCompUsername = identity.getNickname();
        mCompUserAvatar = identity.getAvatarUrl();
        mUserPending = identity.isPending();
    }

    @Bindable
    public boolean isUserPending() {
        return mUserPending;
    }

    @Override
    public int getType() {
        return Type.CREDIT;
    }
}
