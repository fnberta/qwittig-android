/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.identities;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModelBaseImpl;
import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link IdentitiesViewModel}.
 */
public class IdentitiesViewModelImpl
        extends OnlineListViewModelBaseImpl<Identity, IdentitiesViewModel.ViewListener>
        implements IdentitiesViewModel {

    private NumberFormat mMoneyFormatter;

    public IdentitiesViewModelImpl(@Nullable Bundle savedState,
                                   @NonNull IdentitiesViewModel.ViewListener view,
                                   @NonNull RxBus<Object> eventBus,
                                   @NonNull UserRepository userRepository) {
        super(savedState, view, eventBus, userRepository);

        if (savedState != null) {
            mItems = new ArrayList<>();
        }
    }

    @Override
    @Bindable
    public String getCurrentUserBalance() {
        final BigFraction balance = mCurrentIdentity.getBalance();
        mView.setColorTheme(balance);
        return mMoneyFormatter.format(balance);
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mUserRepo.fetchIdentityData(mCurrentIdentity)
                .flatMapObservable(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        final String currency = mCurrentIdentity.getGroup().getCurrency();
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, true, true);
                        return mUserRepo.getIdentities(identity.getGroup(), true);
                    }
                })
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return !Objects.equals(identity.getObjectId(), mCurrentIdentity.getObjectId());
                    }
                })
                .toSortedList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Identity>>() {
                    @Override
                    public void onSuccess(List<Identity> identities) {
                        mItems.clear();
                        mItems.addAll(identities);
                        setLoading(false);
                        mView.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable error) {
                        setLoading(false);
                        mView.showMessage(R.string.toast_error_users_load);
                    }
                })
        );
    }

    @Override
    public void onDataUpdated(boolean successful) {
        setRefreshing(false);
        if (successful) {
            notifyPropertyChanged(BR.currentUserBalance);
            loadData();
        } else {
            mView.showMessageWithAction(R.string.toast_error_users_update, getRefreshAction());
        }
    }

    @Override
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        mView.startUpdateIdentitiesService();
    }

    @NonNull
    private MessageAction getRefreshAction() {
        return new MessageAction(R.string.action_retry) {
            @Override
            public void onClick(View v) {
                refreshItems();
            }
        };
    }

    @Override
    protected void onIdentitySelected(@NonNull Identity identitySelected) {
        super.onIdentitySelected(identitySelected);

        notifyPropertyChanged(BR.currentIdentityBalance);
    }
}
