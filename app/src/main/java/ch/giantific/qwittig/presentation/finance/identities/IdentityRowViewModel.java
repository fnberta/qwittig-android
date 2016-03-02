/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.identities;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.viewmodels.IdentityAvatarRowBaseViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Provides a view model for an identity row, showing the avatar, name and balance.
 */
public class IdentityRowViewModel extends IdentityAvatarRowBaseViewModel {

    private final NumberFormat mMoneyFormatter;
    private String mIdentityBalance;
    private boolean mBalancePositive;

    public IdentityRowViewModel(@NonNull Identity identity) {
        super(identity);

        final String currency = identity.getGroup().getCurrency();
        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);
    }

    @Override
    protected void setIdentity(@NonNull Identity identity) {
        super.setIdentity(identity);

        final BigFraction balance = identity.getBalance();
        mIdentityBalance = mMoneyFormatter.format(balance);
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
