/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.unpaid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.CompensationRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidItemModel;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompsUnpaidItemModel.ViewType;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link CompsUnpaidViewModel}.
 */
public class CompsUnpaidViewModelImpl extends ListViewModelBaseImpl<CompsUnpaidItemModel, CompsUnpaidViewModel.ViewListener>
        implements CompsUnpaidViewModel {

    private static final String STATE_COMP_CHANGE_AMOUNT = "STATE_COMP_CHANGE_AMOUNT";
    private final CompensationRepository mCompsRepo;
    private String mCurrentGroupId;
    private String mCompConfirmingId;
    private String mGroupCurrency;
    private NumberFormat mMoneyFormatter;

    public CompsUnpaidViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepository,
                                    @NonNull CompensationRepository compsRepository) {
        super(savedState, navigator, eventBus, userRepository);

        mCompsRepo = compsRepository;

        if (savedState != null) {
            mCompConfirmingId = savedState.getString(STATE_COMP_CHANGE_AMOUNT);
        }
    }

    @Override
    protected Class<CompsUnpaidItemModel> getItemModelClass() {
        return CompsUnpaidItemModel.class;
    }

    @Override
    protected int compareItemModels(CompsUnpaidItemModel o1, CompsUnpaidItemModel o2) {
        return o1.compareTo(o2);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        if (!TextUtils.isEmpty(mCompConfirmingId)) {
            outState.putString(STATE_COMP_CHANGE_AMOUNT, mCompConfirmingId);
        }
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        mGroupCurrency = identity.getGroupCurrency();
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(mGroupCurrency, true, true);

                        mInitialDataLoaded = false;
                        final String identityId = identity.getId();
                        final String groupId = identity.getGroup();
                        if (!Objects.equals(mCurrentGroupId, groupId)) {
                            mItems.clear();
                        }
                        mCurrentGroupId = groupId;
                        addDataListener(identityId);
                        loadInitialData(identityId);
                    }
                })
        );
    }

    private void addDataListener(@NonNull final String identityId) {
        setDataListenerSub(mCompsRepo.observeCompensationChildren(mCurrentGroupId, identityId, false)
                .filter(new Func1<RxChildEvent<Compensation>, Boolean>() {
                    @Override
                    public Boolean call(RxChildEvent<Compensation> compensationRxChildEvent) {
                        return mInitialDataLoaded;
                    }
                })
                .flatMap(new Func1<RxChildEvent<Compensation>, Observable<CompsUnpaidItemModel>>() {
                    @Override
                    public Observable<CompsUnpaidItemModel> call(final RxChildEvent<Compensation> event) {
                        return getItemModels(event.getValue(), event.getEventType(), identityId);
                    }
                })
                .subscribe(this)
        );
    }

    private void loadInitialData(@NonNull final String identityId) {
        getSubscriptions().add(mCompsRepo.getCompensations(mCurrentGroupId, identityId, false)
                .flatMap(new Func1<Compensation, Observable<CompsUnpaidItemModel>>() {
                    @Override
                    public Observable<CompsUnpaidItemModel> call(Compensation compensation) {
                        return getItemModels(compensation, -1, identityId);
                    }
                })
                .toList()
                .subscribe(new Subscriber<List<CompsUnpaidItemModel>>() {
                    @Override
                    public void onCompleted() {
                        mInitialDataLoaded = true;
                        setLoading(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onDataError(e);
                    }

                    @Override
                    public void onNext(List<CompsUnpaidItemModel> compsUnpaidItemModels) {
                        mItems.addAll(compsUnpaidItemModels);
                    }
                })
        );
    }

    @NonNull
    private Observable<CompsUnpaidItemModel> getItemModels(@NonNull final Compensation compensation,
                                                           final int eventType,
                                                           @NonNull String identityId) {
        final String creditorId = compensation.getCreditor();
        final boolean isCredit = Objects.equals(creditorId, identityId);
        return mUserRepo.getIdentity(isCredit ? compensation.getDebtor() : creditorId)
                .map(new Func1<Identity, CompsUnpaidItemModel>() {
                    @Override
                    public CompsUnpaidItemModel call(Identity identity) {
                        return new CompsUnpaidItemModel(eventType, compensation, identity,
                                mMoneyFormatter, isCredit);
                    }
                })
                .toObservable();
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        mView.showMessage(R.string.toast_error_comps_load);
    }

    @Override
    public void onConfirmButtonClick(@NonNull CompsUnpaidItemModel itemModel) {
        final BigFraction amount = itemModel.getAmountFraction();
        mCompConfirmingId = itemModel.getId();
        mView.showCompensationAmountConfirmDialog(amount, itemModel.getNickname(), mGroupCurrency);
    }

    @Override
    public void onAmountConfirmed(double confirmedAmount) {
        for (int i = 0, itemsSize = mItems.size(); i < itemsSize; i++) {
            final CompsUnpaidItemModel itemModel = mItems.get(i);
            if (itemModel.getViewType() != ViewType.CREDIT || !Objects.equals(itemModel.getId(), mCompConfirmingId)) {
                continue;
            }

            BigFraction amount = itemModel.getAmountFraction();
            boolean amountChanged = false;
            if (Math.abs(amount.doubleValue() - confirmedAmount) >= MoneyUtils.MIN_DIFF) {
                amount = new BigFraction(confirmedAmount);
                amountChanged = true;
            }
            confirmCompensation(itemModel, amount, amountChanged);
            return;
        }
    }

    private void confirmCompensation(@NonNull CompsUnpaidItemModel itemModel,
                                     @NonNull BigFraction amount, final boolean amountChanged) {
        getSubscriptions().add(mCompsRepo.confirmAmountAndAccept(itemModel.getId(), amount, amountChanged)
                .subscribe(new SingleSubscriber<Compensation>() {
                    @Override
                    public void onSuccess(Compensation compensation) {
                        mView.showMessage(R.string.toast_compensation_accepted);
                        if (amountChanged) {
                            // new compensations are calculated on the server
                            setLoading(true);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to confirm compensation with error:");
                        mView.showMessage(R.string.toast_error_comps_paid);
                    }
                })
        );
    }

    @Override
    public void onRemindButtonClick(@NonNull CompsUnpaidItemModel itemModel) {
        mCompsRepo.remindDebtor(itemModel.getId());
        mView.showMessage(R.string.toast_compensation_reminded_user, itemModel.getNickname());
    }
}
