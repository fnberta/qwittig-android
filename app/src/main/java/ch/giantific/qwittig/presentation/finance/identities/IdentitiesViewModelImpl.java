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

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModelBaseImpl;
import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Single;
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
                                   @NonNull IdentityRepository identityRepository,
                                   @NonNull UserRepository userRepository) {
        super(savedState, view, identityRepository, userRepository);

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
        getSubscriptions().add(mIdentityRepo.fetchIdentityDataAsync(mCurrentIdentity)
                .flatMapObservable(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        final String currency = mCurrentIdentity.getGroup().getCurrency();
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, true, true);
                        return mIdentityRepo.getIdentitiesLocalAsync(identity.getGroup(), true);
                    }
                })
                .filter(new Func1<Identity, Boolean>() {
                    @Override
                    public Boolean call(Identity identity) {
                        return !identity.getObjectId().equals(mCurrentIdentity.getObjectId());
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
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        setRefreshing(true);
        mView.loadUpdateUsersWorker();
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
    public void setIdentitiesUpdateStream(@NonNull Observable<Identity> observable,
                                          @NonNull final String workerTag) {
        getSubscriptions().add(observable.toSingle()
                .subscribe(new SingleSubscriber<Identity>() {
                    @Override
                    public void onSuccess(Identity identity) {
                        mView.removeWorker(workerTag);
                        setRefreshing(false);

                        notifyPropertyChanged(BR.currentUserBalance);
                        loadData();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        setRefreshing(false);
                        mView.showMessageWithAction(mUserRepo.getErrorMessage(error),
                                getRefreshAction());
                    }
                })
        );
    }

    @Override
    public void onIdentitySelected() {
        super.onIdentitySelected();

        notifyPropertyChanged(BR.currentIdentityBalance);
    }

    @Override
    public int getItemViewType(int position) {
        throw new UnsupportedOperationException("there is only one view type for this view");
    }
}
