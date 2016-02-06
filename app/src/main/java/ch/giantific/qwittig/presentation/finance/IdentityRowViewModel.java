/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.UserAvatarRowBaseViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 18.01.16.
 */
public class IdentityRowViewModel extends UserAvatarRowBaseViewModel {

    private String mIdentityBalance;
    private boolean mBalancePositive;

    public IdentityRowViewModel(@NonNull Identity identity) {
        super(identity);
    }

    @Override
    protected void setIdentity(@NonNull Identity identity) {
        super.setIdentity(identity);

        final BigFraction balance = identity.getBalance();
        mIdentityBalance = MoneyUtils.formatMoneyNoSymbol(balance, identity.getGroup().getCurrency());
        mBalancePositive = Utils.isPositive(balance);
    }

    @Override
    public void updateIdentity(@NonNull Identity identity) {
        super.updateIdentity(identity);

        notifyChange();
    }

    @Bindable
    public String getIdentityBalance() {
        return mIdentityBalance;
    }

    @Bindable
    public boolean isBalancePositive() {
        return mBalancePositive;
    }
}
