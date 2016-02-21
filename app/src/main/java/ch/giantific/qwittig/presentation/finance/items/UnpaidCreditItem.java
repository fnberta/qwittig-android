/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.items;

import android.support.annotation.NonNull;

import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;

/**
 * Created by fabio on 11.02.16.
 */
public class UnpaidCreditItem extends UnpaidCompItem {

    public UnpaidCreditItem(@NonNull Compensation compensation, @NonNull NumberFormat moneyformatter) {
        super(compensation, moneyformatter);

        mCredit = true;
        final Identity identity = compensation.getDebtor();
        mCompUsername = identity.getNickname();
        mCompUserAvatar = identity.getAvatarUrl();
    }

    @Override
    public int getType() {
        return Type.CREDIT;
    }
}
