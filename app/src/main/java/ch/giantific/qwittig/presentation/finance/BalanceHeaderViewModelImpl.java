/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Defines an observable view model for the header showing the user's balance.
 */
public class BalanceHeaderViewModelImpl extends ViewModelBaseImpl<BalanceHeaderViewModel.ViewListener>
        implements BalanceHeaderViewModel {

    private String balance;
    private NumberFormat moneyFormatter;

    public BalanceHeaderViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull Navigator navigator,
                                      @NonNull RxBus<Object> eventBus,
                                      @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);
    }

    @Override
    @Bindable
    public String getBalance() {
        return balance;
    }

    @Override
    public void setBalance(@NonNull BigFraction balance) {
        this.balance = moneyFormatter.format(balance);
        notifyPropertyChanged(BR.balance);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .flatMap(user -> userRepo.observeIdentity(user.getCurrentIdentity()))
                .doOnNext(identity -> moneyFormatter = MoneyUtils.getMoneyFormatter(identity.getGroupCurrency(), true, true))
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        view.showMessage(R.string.toast_error_balance_load);
                    }

                    @Override
                    public void onNext(Identity identity) {
                        final BigFraction balance = identity.getBalanceFraction();
                        setBalance(balance);
                        view.setColorTheme(balance);
                    }
                })
        );
    }
}
