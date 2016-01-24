/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fabio on 19.01.16.
 */
public class FinanceCompsUnpaidViewModelImpl extends OnlineListViewModelBaseImpl<Compensation, FinanceCompsUnpaidViewModel.ViewListener>
        implements FinanceCompsUnpaidViewModel {

    private static final String STATE_COMPS_LOADING = "STATE_COMPS_LOADING";
    private static final String STATE_COMP_CHANGE_AMOUNT = "STATE_COMP_CHANGE_AMOUNT";
    private CompensationRepository mCompsRepo;
    private ArrayList<String> mLoadingComps;
    private String mCompChangeAmount;

    public FinanceCompsUnpaidViewModelImpl(@Nullable Bundle savedState,
                                           @NonNull GroupRepository groupRepo,
                                           @NonNull UserRepository userRepository,
                                           @NonNull CompensationRepository compsRepo) {
        super(savedState, groupRepo, userRepository);

        mCompsRepo = compsRepo;

        if (savedState != null) {
            mLoadingComps = savedState.getStringArrayList(STATE_COMPS_LOADING);
            mCompChangeAmount = savedState.getString(STATE_COMP_CHANGE_AMOUNT);
        } else {
            mLoadingComps = new ArrayList<>();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putStringArrayList(STATE_COMPS_LOADING, mLoadingComps);
        if (!TextUtils.isEmpty(mCompChangeAmount)) {
            outState.putString(STATE_COMP_CHANGE_AMOUNT, mCompChangeAmount);
        }
    }

    @Override
    public void updateList() {
        mSubscriptions.add(mGroupRepo.fetchGroupDataAsync(mCurrentGroup)
                .toObservable()
                .flatMap(new Func1<Group, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(Group group) {
                        return mCompsRepo.getCompensationsLocalUnpaidAsync(mCurrentUser, group);
                    }
                })
                .subscribe(new Subscriber<Compensation>() {
                    List<Compensation> compsPending;
                    List<Compensation> compsSuggested;

                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                        compsPending = new ArrayList<>();
                        compsSuggested = new ArrayList<>();
                    }

                    @Override
                    public void onCompleted() {
                        mItems.add(null);
                        mItems.addAll(compsPending);
                        mItems.add(null);
                        mItems.addAll(compsSuggested);

                        setLoading(false);
                        mView.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showMessage(R.string.toast_error_comps_load);
                    }

                    @Override
                    public void onNext(Compensation compensation) {
                        if (compensation.isPending()) {
                            compsPending.add(compensation);
                            compensation.setIsLoading(mLoadingComps.contains(compensation.getObjectId()));
                        } else {
                            compsSuggested.add(compensation);
                        }
                    }
                })
        );
    }

    @Override
    public int getItemViewType(int position) {
        final Compensation compensation = mItems.get(position);
        if (compensation == null) {
            final int count = getItemCount();
            if (count > 2 && position == count - 1) {
                return TYPE_SUGGESTION_LOADING;
            }

            return TYPE_HEADER;
        }

        if (compensation.isPending()) {
            final User beneficiary = compensation.getBeneficiary();
            if (beneficiary.getObjectId().equals(mCurrentUser.getObjectId())) {
                return TYPE_PENDING_POS;
            }

            return TYPE_PENDING_NEG;
        }

        return TYPE_SUGGESTION;
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
                        updateList();
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
    public void onClaimButtonClick(int position) {
        final Compensation compensation = mItems.get(position);
        compensation.setPending(true);
        compensation.saveEventually();
        // TODO: update adapter nicely instead of just reloading (position changed massively)
        updateList();
    }

    @Override
    public void onConfirmButtonClick(int position) {
        final Compensation compensation = mItems.get(position);
        // TODO: unpin and pin with new label
        compensation.setPaid(true);
        compensation.saveEventually();

        mItems.remove(position);
        mView.notifyItemRemoved(position);
        mView.showMessage(R.string.toast_compensation_accepted);

        setNotPendingCompsLoading();
    }

    private void setNotPendingCompsLoading() {
        int counter = 0;
        for (Iterator<Compensation> iterator = mItems.iterator(); iterator.hasNext(); ) {
            final Compensation comp = iterator.next();
            if (!comp.isPending()) {
                iterator.remove();
                mView.notifyItemRemoved(counter);
            }

            counter++;
        }

        mItems.add(null);
        mView.notifyItemInserted(getItemCount() - 1);
    }

    @Override
    public void onRemindButtonClick(int position) {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        final Compensation compensation = mItems.get(position);
        final String compensationId = compensation.getObjectId();
        if (mLoadingComps.contains(compensationId)) {
            return;
        }

        setCompensationLoading(compensation, compensationId, position, true);
        mView.loadCompensationRemindWorker(compensationId); // TODO: handle tags correctly, there could be multiple workers
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
    public void setCompensationReminderStream(@NonNull Single<String> single,
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
    private Compensation stopCompensationLoading(@NonNull String objectId) {
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final Compensation compensation = mItems.get(i);
            if (objectId.equals(compensation.getObjectId())) {
                setCompensationLoading(compensation, objectId, i, false);
                return compensation;
            }
        }

        return null;
    }

    @Override
    public void onChangeAmountButtonClick(int position) {
        final Compensation comp = mItems.get(position);
        final BigFraction amount = comp.getAmountFraction();
        final String currency = mCurrentGroup.getCurrency();
        mView.showChangeCompensationAmountDialog(amount, currency);

        mCompChangeAmount = comp.getObjectId();
    }

    @Override
    public void onChangedAmountSet(@NonNull BigFraction amount) {
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final Compensation comp = mItems.get(i);
            if (comp.getObjectId().equals(mCompChangeAmount)) {
                comp.setAmountFraction(amount);
                comp.saveEventually();

                mView.notifyItemChanged(i);
                setNotPendingCompsLoading();
                return;
            }
        }
    }

    @Override
    public void onNewGroupSet() {
        super.onNewGroupSet();

        updateList();
    }
}
