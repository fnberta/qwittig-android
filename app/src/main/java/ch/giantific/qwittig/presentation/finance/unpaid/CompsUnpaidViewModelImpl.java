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
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.bus.events.EventCompensationConfirmed;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.viewmodels.OnlineListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidCompCreditItemModel;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidCompDebtItemModel;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidCompItemModel;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidHeaderItemModel;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidItemModel;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidItemModel.Type;
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
        extends OnlineListViewModelBaseImpl<CompsUnpaidItemModel, CompsUnpaidViewModel.ViewListener>
        implements CompsUnpaidViewModel {

    private static final String STATE_COMPS_LOADING = "STATE_COMPS_LOADING";
    private static final String STATE_COMP_CHANGE_AMOUNT = "STATE_COMP_CHANGE_AMOUNT";
    private final CompensationRepository mCompsRepo;
    private final ArrayList<String> mLoadingComps;
    private String mCompConfirmingId;
    private NumberFormat mMoneyFormatter;

    public CompsUnpaidViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepository,
                                    @NonNull CompensationRepository compsRepo) {
        super(savedState, eventBus, userRepository);

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
                            mItems.add(new CompsUnpaidHeaderItemModel(R.string.header_comps_credits));
                            for (Compensation comp : credits) {
                                final boolean loading = mLoadingComps.contains(comp.getObjectId());
                                mItems.add(new CompsUnpaidCompCreditItemModel(comp, mMoneyFormatter, loading));
                            }
                        }

                        if (!debts.isEmpty()) {
                            mItems.add(new CompsUnpaidHeaderItemModel(R.string.header_comps_debts));
                            for (Compensation comp : debts) {
                                final boolean loading = mLoadingComps.contains(comp.getObjectId());
                                mItems.add(new CompsUnpaidCompDebtItemModel(comp, mMoneyFormatter, loading));
                            }
                        }

                        setLoading(false);
                        mListInteraction.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        setLoading(false);
                        mView.showMessage(R.string.toast_error_comps_load);
                    }

                    @Override
                    public void onNext(Compensation compensation) {
                        if (Objects.equals(compensation.getDebtor().getObjectId(), currentIdentityId)) {
                            debts.add(compensation);
                        } else {
                            credits.add(compensation);
                        }
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
        for (CompsUnpaidItemModel item : mItems) {
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
    public void onConfirmButtonClick(@NonNull CompsUnpaidCompCreditItemModel itemModel) {
        final BigFraction amount = itemModel.getCompAmountRaw();
        final String currency = mCurrentIdentity.getGroup().getCurrency();

        mCompConfirmingId = itemModel.getId();
        mView.showCompensationAmountConfirmDialog(amount, itemModel.getCompUsername(), currency);
    }

    @Override
    public void onAmountConfirmed(double amount) {
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final CompsUnpaidItemModel itemModel = mItems.get(i);
            if (itemModel.getType() != Type.CREDIT) {
                continue;
            }

            final CompsUnpaidCompItemModel compsUnpaidItemModel = (CompsUnpaidCompItemModel) itemModel;
            if (Objects.equals(compsUnpaidItemModel.getId(), mCompConfirmingId)) {
                boolean amountChanged = false;
                final BigFraction originalAmount = compsUnpaidItemModel.getCompAmountRaw();
                final double diff = originalAmount.doubleValue() - amount;
                final Compensation compensation = compsUnpaidItemModel.getCompensation();
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
                            mListInteraction.notifyItemRemoved(position);
                            notifyPropertyChanged(BR.empty);
                        }

                        mEventBus.post(new EventCompensationConfirmed());
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_comps_paid);
                    }
                });
    }

    @Override
    public void onRemindButtonClick(@NonNull CompsUnpaidCompCreditItemModel itemModel) {
        if (!mView.isNetworkAvailable()) {
            mView.showMessage(R.string.toast_no_connection);
            return;
        }

        final String objectId = itemModel.getId();
        if (mLoadingComps.contains(objectId)) {
            return;
        }

        final int pos = mItems.indexOf(itemModel);
        setCompensationLoading(itemModel, objectId, pos, true);
        mView.loadCompensationRemindWorker(objectId);
    }

    private void setCompensationLoading(@NonNull CompsUnpaidCompItemModel compsUnpaidItem,
                                        @NonNull String objectId, int position, boolean itemLoading) {
        compsUnpaidItem.setItemLoading(itemLoading);
        mListInteraction.notifyItemChanged(position);

        if (itemLoading) {
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
                final CompsUnpaidCompCreditItemModel itemModel = stopCompensationLoading(compensationId);
                if (itemModel != null) {
                    mView.showMessage(R.string.toast_compensation_reminded_user, itemModel.getCompUsername());
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
    private CompsUnpaidCompCreditItemModel stopCompensationLoading(@NonNull String compId) {
        for (int i = 0, size = mItems.size(); i < size; i++) {
            final CompsUnpaidItemModel item = mItems.get(i);
            if (item.getType() == Type.HEADER) {
                continue;
            }

            final CompsUnpaidCompItemModel unpaidItem = (CompsUnpaidCompItemModel) item;
            if (Objects.equals(compId, unpaidItem.getId())) {
                setCompensationLoading(unpaidItem, compId, i, false);
                return (CompsUnpaidCompCreditItemModel) unpaidItem;
            }
        }

        return null;
    }
}
