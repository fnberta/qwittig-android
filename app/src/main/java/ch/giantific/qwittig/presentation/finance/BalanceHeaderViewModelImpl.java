/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.SingleSubscriber;

/**
 * Defines an observable view model for the header showing the user's balance.
 */
public class BalanceHeaderViewModelImpl extends ViewModelBaseImpl<BalanceHeaderViewModel.ViewListener>
        implements BalanceHeaderViewModel {

    private NumberFormat mMoneyFormatter;

    public BalanceHeaderViewModelImpl(@Nullable Bundle savedState,
                                      @NonNull RxBus<Object> eventBus,
                                      @NonNull UserRepository userRepository) {
        super(savedState, eventBus, userRepository);
    }

    @Override
    @Bindable
    public String getCurrentIdentityBalance() {
        if (mMoneyFormatter != null) {
            final BigFraction balance = mCurrentIdentity.getBalance();
            mView.setColorTheme(balance);
            return mMoneyFormatter.format(balance);
        }

        return "";
    }

    @Override
    public void onViewVisible() {
        super.onViewVisible();

        loadData();
    }

    private void loadData() {
        getSubscriptions().add(mUserRepo.fetchIdentityData(mCurrentIdentity)
                .subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity value) {
                        final String currency = mCurrentIdentity.getGroup().getCurrency();
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, true, true);
                        notifyPropertyChanged(BR.currentIdentityBalance);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_balance_load);
                    }
                })
        );
    }

    @Override
    public void onDataUpdated(boolean successful) {
        if (successful) {
            notifyPropertyChanged(BR.currentIdentityBalance);
        }
    }

    @Override
    protected void onIdentitySelected(@NonNull Identity identitySelected) {
        super.onIdentitySelected(identitySelected);

        loadData();
    }
}
