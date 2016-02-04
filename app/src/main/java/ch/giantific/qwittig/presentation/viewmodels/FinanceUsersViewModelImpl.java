/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.SingleSubscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 18.01.16.
 */
public class FinanceUsersViewModelImpl
        extends OnlineListViewModelBaseImpl<User, FinanceUsersViewModel.ViewListener>
        implements FinanceUsersViewModel {

    public FinanceUsersViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull FinanceUsersViewModel.ViewListener view,
                                     @NonNull GroupRepository groupRepo,
                                     @NonNull UserRepository userRepository) {
        super(savedState, view, groupRepo, userRepository);

        if (savedState != null) {
            mItems = new ArrayList<>();
        }
    }

    @Override
    @Bindable
    public String getCurrentUserBalance() {
        final BigFraction balance = mCurrentUser.getBalance(mCurrentGroup);
        mView.setColorTheme(balance);
        return MoneyUtils.formatMoney(balance, mCurrentGroup.getCurrency());
    }

    @Override
    public void loadData() {
        mSubscriptions.add(mGroupRepo.fetchGroupDataAsync(mCurrentGroup)
                .toObservable()
                .flatMap(new Func1<Group, Observable<User>>() {
                    @Override
                    public Observable<User> call(Group group) {
                        return mUserRepo.getUsersLocalAsync(group);
                    }
                })
                .filter(new Func1<User, Boolean>() {
                    @Override
                    public Boolean call(User user) {
                        return !user.getObjectId().equals(mCurrentUser.getObjectId());
                    }
                })
                .toSortedList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        mItems.clear();
                        mItems.addAll(users);
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
    public void setUsersUpdateStream(@NonNull Observable<User> observable,
                                     @NonNull final String workerTag) {
        mSubscriptions.add(observable.toSingle()
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(User value) {
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
    public void onNewGroupSet() {
        super.onNewGroupSet();

        notifyPropertyChanged(BR.currentUserBalance);
        loadData();
    }

    @Override
    public int getItemViewType(int position) {
        throw new UnsupportedOperationException("there is only one view type for this view");
    }
}
