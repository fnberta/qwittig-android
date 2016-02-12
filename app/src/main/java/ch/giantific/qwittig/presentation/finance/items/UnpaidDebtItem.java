/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.items;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;

/**
 * Created by fabio on 11.02.16.
 */
public class UnpaidDebtItem extends UnpaidCompItem {

    public UnpaidDebtItem(@NonNull Compensation compensation, @NonNull String currency) {
        super(compensation, currency);

        mCredit = false;
        final Identity identity = compensation.getCreditor();
        mCompUsername = identity.getNickname();
        mCompUserAvatar = identity.getAvatarUrl();
    }

    @Override
    public int getType() {
        return Type.DEBT;
    }
}
