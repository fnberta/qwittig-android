/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.items;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;

/**
 * Created by fabio on 11.02.16.
 */
public class UnpaidDebtItem extends UnpaidCompItem {

    private final boolean mUserPending;

    public UnpaidDebtItem(@NonNull Compensation compensation, @NonNull NumberFormat moneyFormatter) {
        super(compensation, moneyFormatter);

        mCredit = false;
        final Identity identity = compensation.getCreditor();
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
        return Type.DEBT;
    }
}
