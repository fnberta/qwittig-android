/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.finance.unpaid.items.CompsUnpaidBaseItem;
import ch.giantific.qwittig.presentation.finance.unpaid.items.CompsUnpaidBaseItem.Type;
import ch.giantific.qwittig.presentation.finance.unpaid.items.CompsUnpaidCreditItem;
import ch.giantific.qwittig.presentation.finance.unpaid.items.CompsUnpaidDebtItem;
import ch.giantific.qwittig.presentation.finance.unpaid.items.CompsUnpaidHeaderItem;
import ch.giantific.qwittig.presentation.finance.unpaid.items.CompsUnpaidItem;
import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link CompsUnpaidViewModel}.
 */
public class CompsUnpaidViewModelImpl
        extends OnlineListViewModelBaseImpl<CompsUnpaidBaseItem, CompsUnpaidViewModel.ViewListener>
        implements CompsUnpaidViewModel {

    private static final String STATE_COMPS_LOADING = "STATE_COMPS_LOADING";
    private static final String STATE_COMP_CHANGE_AMOUNT = "STATE_COMP_CHANGE_AMOUNT";
    private final CompensationRepository mCompsRepo;
    private final ArrayList<String> mLoadingComps;
    private String mCompConfirmingId;
    private NumberFormat mMoneyFormatter;

    public CompsUnpaidViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull CompsUnpaidViewModel.ViewListener view,
                                    @NonNull UserRepository userRepository,
                                    @NonNull CompensationRepository compsRepo) {
        super(savedState, view, userRepository);

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
    public void loadData() {
        getSubscriptions().add(mUserRepo.fetchIdentityData(mCurrentIdentity)
                .flatMapObservable(new Func1<Identity, Observable<Compensation>>() {
                    @Override
                    public Observable<Compensation> call(Identity identity) {
                        final String currency = mCurrentIdentity.getGroup().getCurrency();
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, true, true);
                        return mCompsRepo.getCompensationsUnpaid(identity);
                    }
                })
                .subscribe(new Subscriber<Compensation>() {
                    private final String currentIdentityId = mCurrentIdentity.getObjectId();
                    private final List<Compensation> credits = new ArrayList<>();
                    private final List<Compensation> debts = new ArrayList<>();

                    @Override
                    public void onStart() {
                        super.onStart();
                        mItems.clear();
                    }

                    @Override
                    public void onCompleted() {
                        if (!credits.isEmpty()) {
                            mItems.add(new CompsUnpaidHeaderItem(R.string.header_comps_credits));
                            for (Compensation comp : credits) {
                                mItems.add(new CompsUnpaidCreditItem(comp, mMoneyFormatter));
                            }
                        }

                        if (!debts.isEmpty()) {
                            mItems.add(new CompsUnpaidHeaderItem(R.string.header_comps_debts));
                            for (Compensation comp : debts) {
                                mItems.add(new CompsUnpaidDebtItem(comp, mMoneyFormatter));
                            }
                        }

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
                        if (compensation.getDebtor().getObjectId().equals(currentIdentityId)) {
                            debts.add(compensation);
                        } else {
                            credits.add(compensation);
                        }
                        compensation.setLoading(mLoadingComps.contains(compensation.getObjectId()));
                    }
                })
        );
    }

    @Override
    public void onDataUpdated(boolean successful) {
        setRefreshing(false);
        if (successful) {
            loadData();
        } else {
            mView.showMessageWithAction(R.string.toast_error_comps_update, getRefreshAction());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public boolean isEmpty() {
        for (CompsUnpaidBaseItem item : mItems) {
            final int type = item.getType();
            if (type == Type.CREDIT || type == Type.DEBT) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void refreshItems() {
        if (!mView.isNetworkAvailable()) {
            setRefreshing(false);
            mView.showMessageWithAction(R.string.toast_no_connection, getRefreshAction());
            return;
        }

        mView.startUpdateCompensationsUnpaidService();
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
    public void onConfirmButtonClick(int position) {
        final CompsUnpaidItem unpaidItem = (CompsUnpaidItem) mItems.get(position);
        final Compensation comp = unpaidItem.getCompensation();
        final BigFraction amount = comp.getAmountFraction();
        final String currency = mCurrentIdentity.getGroup().getCurrency();

        mCompConfirmingId = comp.getObjectId();
        mView.showCompensationAmountConfirmDialog(amount, comp.getDebtor().getNickname(), currency);
    }

    @Override
    public void onAmountConfirmed(double amount) {
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final CompsUnpaidBaseItem item = mItems.get(i);
            if (item.getType() != Type.CREDIT) {
                continue;
            }

            final Compensation compensation = ((CompsUnpaidItem) item).getCompensation();
            if (compensation.getObjectId().equals(mCompConfirmingId)) {
                boolean amountChanged = false;
                final BigFraction originalAmount = compensation.getAmountFraction();
                final double diff = originalAmount.doubleValue() - amount;
                if (Math.abs(diff) >= MoneyUtils.MIN_DIFF) {
                    compensation.setAmountFraction(new BigFraction(amount));
                    amountChanged = true;
                }
                confirmCompensation(compensation, i, amountChanged);
                return;
            }
        }
    }

    private void confirmCompensation(@NonNull Compensation compensation, final int position,
                                     final boolean amountChanged) {
        mCompsRepo.saveCompensationPaid(compensation)
                .subscribe(new SingleSubscriber<Compensation>() {
                    @Override
                    public void onSuccess(Compensation compensation) {
                        mView.showMessage(R.string.toast_compensation_accepted);

                        if (amountChanged) {
                            // new compensations are calculated on the server
                            setLoading(true);
                        } else {
                            mItems.remove(position);
                            mView.notifyItemRemoved(position);
                            notifyPropertyChanged(BR.empty);
                        }

                        mView.onCompensationConfirmed();
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

        final CompsUnpaidItem unpaidItem = (CompsUnpaidItem) mItems.get(position);
        final Compensation compensation = unpaidItem.getCompensation();
        final String compensationId = compensation.getObjectId();
        if (mLoadingComps.contains(compensationId)) {
            return;
        }

        setCompensationLoading(compensation, compensationId, position, true);
        mView.loadCompensationRemindWorker(compensationId);
    }

    private void setCompensationLoading(@NonNull Compensation compensation,
                                        @NonNull String objectId, int position, boolean isLoading) {
        compensation.setLoading(isLoading);
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
        getSubscriptions().add(single.subscribe(new SingleSubscriber<String>() {
            @Override
            public void onSuccess(String value) {
                final Compensation compensation = stopCompensationLoading(compensationId);
                if (compensation != null) {
                    final Identity debtor = compensation.getDebtor();
                    final String nickname = debtor.getNickname();
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
            final CompsUnpaidBaseItem item = mItems.get(i);
            if (item.getType() == Type.HEADER) {
                continue;
            }

            final CompsUnpaidItem unpaidItem = (CompsUnpaidItem) item;
            final Compensation compensation = unpaidItem.getCompensation();
            if (compId.equals(compensation.getObjectId())) {
                setCompensationLoading(compensation, compId, i, false);
                return compensation;
            }
        }

        return null;
    }
}
