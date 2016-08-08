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
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Defines an observable view model for the header showing the user's balance.
 */
public class BalanceHeaderViewModelImpl extends ViewModelBaseImpl<BalanceHeaderViewModel.ViewListener>
        implements BalanceHeaderViewModel {

    private String mBalance;
    private NumberFormat mMoneyFormatter;

    public BalanceHeaderViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull Navigator navigator,
                                      @NonNull RxBus<Object> eventBus,
                                      @NonNull UserRepository userRepository) {
        super(savedState, navigator, eventBus, userRepository);
    }

    @Override
    @Bindable
    public String getBalance() {
        return mBalance;
    }

    @Override
    public void setBalance(@NonNull BigFraction balance) {
        mBalance = mMoneyFormatter.format(balance);
        notifyPropertyChanged(BR.balance);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return mUserRepo.observeIdentity(user.getCurrentIdentity());

                    }
                })
                .doOnNext(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(identity.getGroupCurrency(), true, true);
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        mView.showMessage(R.string.toast_error_balance_load);
                    }

                    @Override
                    public void onNext(Identity identity) {
                        final BigFraction balance = identity.getBalanceFraction();
                        setBalance(balance);
                        mView.setColorTheme(balance);
                    }
                })
        );
    }
}
