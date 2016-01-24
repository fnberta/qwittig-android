/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.Bindable;
import android.support.annotation.NonNull;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 18.01.16.
 */
public class UserRowViewModel extends UserAvatarRowBaseViewModel {

    private Group mCurrentGroup;

    public UserRowViewModel(@NonNull User user, @NonNull Group currentGroup) {
        super(user);

        mCurrentGroup = currentGroup;
    }

    @Override
    public void setUser(@NonNull User user) {
        super.setUser(user);

        notifyPropertyChanged(BR.userBalance);
        notifyPropertyChanged(BR.userBalancePositive);
    }

    @Bindable
    public String getUserBalance() {
        return MoneyUtils.formatMoneyNoSymbol(mUser.getBalance(mCurrentGroup),
                mCurrentGroup.getCurrency());
    }

    @Bindable
    public boolean isUserBalancePositive() {
        return Utils.isPositive(mUser.getBalance(mCurrentGroup));
    }
}
