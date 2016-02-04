/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.CompensationUnpaidItem;
import ch.giantific.qwittig.domain.models.CompensationUnpaidItem.Type;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 19.01.16.
 */
public class FinanceCompsUnpaidViewModelImpl
        extends OnlineListViewModelBaseImpl<CompensationUnpaidItem, FinanceCompsUnpaidViewModel.ViewListener>
        implements FinanceCompsUnpaidViewModel {

    private static final String STATE_COMPS_LOADING = "STATE_COMPS_LOADING";
    private static final String STATE_COMP_CHANGE_AMOUNT = "STATE_COMP_CHANGE_AMOUNT";
    private CompensationRepository mCompsRepo;
    private ArrayList<String> mLoadingComps;
    private String mCompConfirmingId;

    public FinanceCompsUnpaidViewModelImpl(@Nullable Bundle savedState,
                                           @NonNull FinanceCompsUnpaidViewModel.ViewListener view,
                                           @NonNull GroupRepository groupRepo,
                                           @NonNull UserRepository userRepository,
                                           @NonNull CompensationRepository compsRepo) {
        super(savedState, view, groupRepo, userRepository);

        mCompsRepo = compsRepo;

        if (savedState != null) {
            mItems = new ArrayList<>();
            mLoadingComps = savedState.getStringArrayList(STATE_COMPS_LOADING);
            mCompConfirmingId = savedState.getString(STATE_COMP_CHANGE_AMOUNT);
        } else {
            mLoadingComps = new ArrayList<>();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putStringArrayList(STATE_COMPS_LOADING, mLoadingComps);
        if (!TextUtils.isEmpty(mCompConfirmingId)) {
            outState.putString(STATE_COMP_CHANGE_AMOUNT, mCompConfirmingId);
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
                .flatMapObservable(new Func1<Group, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(Group group) {
                        return mCompsRepo.getCompensationsLocalUnpaidAsync(mCurrentUser, group);
                    }
                })
                .subscribe(new Subscriber<Compensation>() {
                    private String currentUserId = mCurrentUser.getObjectId();
                    private List<Compensation> credits = new ArrayList<>();
                    private List<Compensation> debts = new ArrayList<>();

                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                    }

                    @Override
                    public void onCompleted() {
                        if (!credits.isEmpty()) {
                            mItems.add(CompensationUnpaidItem.createNewHeaderInstance(R.string.header_comps_credits));
                            for (Compensation comp : credits) {
                                mItems.add(CompensationUnpaidItem.createNewCreditInstance(comp));
                            }
                        }

                        if (!debts.isEmpty()) {
                            mItems.add(CompensationUnpaidItem.createNewHeaderInstance(R.string.header_comps_debts));
                            for (Compensation comp : debts) {
                                mItems.add(CompensationUnpaidItem.createNewDebtInstance(comp));
                            }
                        }

                        notifyPropertyChanged(BR.currentUserBalance);
                        setLoading(false);
                        mView.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        setLoading(false);
                        mView.showMessage(R.string.toast_error_comps_load);
                    }

                    @Override
                    public void onNext(Compensation compensation) {
                        if (compensation.getPayer().getObjectId().equals(currentUserId)) {
                            debts.add(compensation);
                        } else {
                            credits.add(compensation);
                        }
                        compensation.setIsLoading(mLoadingComps.contains(compensation.getObjectId()));
                    }
                })
        );
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        mView.loadUpdateCompensationsUnpaidWorker();
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
    public void setCompensationsUpdateStream(@NonNull Observable<Compensation> observable,
                                             boolean paid, @NonNull final String workerTag) {
        mSubscriptions.add(observable
                .toList()
                .toSingle()
                .subscribe(new SingleSubscriber<List<Compensation>>() {
                    @Override
                    public void onSuccess(List<Compensation> value) {
                        mView.removeWorker(workerTag);
                        setRefreshing(false);

                        loadData();
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.removeWorker(workerTag);
                        setRefreshing(false);

                        mView.showMessageWithAction(mCompsRepo.getErrorMessage(error),
                                getRefreshAction());
                    }
                })
        );
    }

    @Override
    public void onConfirmButtonClick(int position) {
        final Compensation comp = mItems.get(position).getCompensation();
        final BigFraction amount = comp.getAmountFraction();
        final String currency = mCurrentGroup.getCurrency();

        mCompConfirmingId = comp.getObjectId();
        mView.showCompensationAmountConfirmDialog(amount, comp.getPayer().getNickname(), currency);
    }

    @Override
    public void onAmountConfirmed(@NonNull BigFraction amount) {
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final CompensationUnpaidItem item = mItems.get(i);
            if (item.getType() != Type.CREDIT) {
                continue;
            }
            final Compensation compensation = item.getCompensation();
            if (compensation.getObjectId().equals(mCompConfirmingId)) {
                compensation.setAmountFraction(amount);
                confirmCompensation(compensation, i);
                return;
            }
        }
    }

    private void confirmCompensation(Compensation compensation, final int position) {
        mCompsRepo.saveCompensationPaid(compensation)
                .subscribe(new SingleSubscriber<Compensation>() {
                    @Override
                    public void onSuccess(Compensation compensation) {
                        mItems.remove(position);
                        mView.notifyItemRemoved(position);
                        mView.showMessage(R.string.toast_compensation_accepted);

                        // new compensations are calculated on the server
                        setLoading(true);
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_comps_paid);
                    }
                });
    }

    @Override
    public void onRemindButtonClick(int position) {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        final Compensation compensation = mItems.get(position).getCompensation();
        final String compensationId = compensation.getObjectId();
        if (mLoadingComps.contains(compensationId)) {
            return;
        }

        setCompensationLoading(compensation, compensationId, position, true);
        mView.loadCompensationRemindWorker(compensationId);
    }

    private void setCompensationLoading(@NonNull Compensation compensation,
                                        @NonNull String objectId, int position, boolean isLoading) {
        compensation.setIsLoading(isLoading);
        mView.notifyItemChanged(position);

        if (isLoading) {
            mLoadingComps.add(objectId);
        } else {
            mLoadingComps.remove(objectId);
        }
    }

    @Override
    public void setCompensationRemindStream(@NonNull Single<String> single,
                                            @NonNull final String compensationId,
                                            @NonNull final String workerTag) {
        mSubscriptions.add(single.subscribe(new SingleSubscriber<String>() {
            @Override
            public void onSuccess(String value) {
                final Compensation compensation = stopCompensationLoading(compensationId);
                if (compensation != null) {
                    final User payer = compensation.getPayer();
                    final String nickname = payer.getNickname();
                    mView.showMessage(R.string.toast_compensation_reminded_user, nickname);
                }
            }

            @Override
            public void onError(Throwable error) {
                mView.removeWorker(workerTag);
                mView.showMessage(mCompsRepo.getErrorMessage(error));
                stopCompensationLoading(compensationId);
            }
        }));
    }

    @Nullable
    private Compensation stopCompensationLoading(@NonNull String compId) {
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final CompensationUnpaidItem item = mItems.get(i);
            if (item.getType() == Type.HEADER) {
                continue;
            }

            final Compensation compensation = item.getCompensation();
            if (compId.equals(compensation.getObjectId())) {
                setCompensationLoading(compensation, compId, i, false);
                return compensation;
            }
        }

        return null;
    }

    @Override
    public void onNewGroupSet() {
        super.onNewGroupSet();

        loadData();
    }
}
