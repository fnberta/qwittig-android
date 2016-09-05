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
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompUnpaidItemModel;
import ch.giantific.qwittig.presentation.finance.unpaid.itemmodels.CompUnpaidItemModel.ViewType;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link CompsUnpaidViewModel}.
 */
public class CompsUnpaidViewModelImpl extends ListViewModelBaseImpl<CompUnpaidItemModel, CompsUnpaidViewModel.ViewListener>
        implements CompsUnpaidViewModel {

    private static final String STATE_COMP_CHANGE_AMOUNT = "STATE_COMP_CHANGE_AMOUNT";
    private final CompensationRepository compsRepo;
    private String currentGroupId;
    private String compConfirmingId;
    private String groupCurrency;
    private NumberFormat moneyFormatter;

    public CompsUnpaidViewModelImpl(@Nullable Bundle savedState,
                                    @NonNull Navigator navigator,
                                    @NonNull RxBus<Object> eventBus,
                                    @NonNull UserRepository userRepos,
                                    @NonNull CompensationRepository compsRepo) {
        super(savedState, navigator, eventBus, userRepos);

        this.compsRepo = compsRepo;

        if (savedState != null) {
            compConfirmingId = savedState.getString(STATE_COMP_CHANGE_AMOUNT);
        }
    }

    @Override
    protected Class<CompUnpaidItemModel> getItemModelClass() {
        return CompUnpaidItemModel.class;
    }

    @Override
    protected int compareItemModels(CompUnpaidItemModel o1, CompUnpaidItemModel o2) {
        return o1.compareTo(o2);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        if (!TextUtils.isEmpty(compConfirmingId)) {
            outState.putString(STATE_COMP_CHANGE_AMOUNT, compConfirmingId);
        }
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(User user) {
                        return userRepo.getIdentity(user.getCurrentIdentity()).toObservable();
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        groupCurrency = identity.getGroupCurrency();
                        moneyFormatter = MoneyUtils.getMoneyFormatter(groupCurrency, true, true);

                        initialDataLoaded = false;
                        final String identityId = identity.getId();
                        final String groupId = identity.getGroup();
                        if (!Objects.equals(currentGroupId, groupId)) {
                            items.clear();
                        }
                        currentGroupId = groupId;
                        addDataListener(identityId);
                        loadInitialData(identityId);
                    }
                })
        );
    }

    private void addDataListener(@NonNull final String identityId) {
        setDataListenerSub(compsRepo.observeCompensationChildren(currentGroupId, identityId, false)
                .filter(new Func1<RxChildEvent<Compensation>, Boolean>() {
                    @Override
                    public Boolean call(RxChildEvent<Compensation> compensationRxChildEvent) {
                        return initialDataLoaded;
                    }
                })
                .flatMap(new Func1<RxChildEvent<Compensation>, Observable<CompUnpaidItemModel>>() {
                    @Override
                    public Observable<CompUnpaidItemModel> call(final RxChildEvent<Compensation> event) {
                        return getItemModel(event.getValue(), event.getEventType(), identityId);
                    }
                })
                .subscribe(this)
        );
    }

    private void loadInitialData(@NonNull final String identityId) {
        getSubscriptions().add(compsRepo.getCompensations(currentGroupId, identityId, false)
                .flatMap(new Func1<Compensation, Observable<CompUnpaidItemModel>>() {
                    @Override
                    public Observable<CompUnpaidItemModel> call(Compensation compensation) {
                        return getItemModel(compensation, EventType.NONE, identityId);
                    }
                })
                .toList()
                .subscribe(new Subscriber<List<CompUnpaidItemModel>>() {
                    @Override
                    public void onCompleted() {
                        initialDataLoaded = true;
                        setLoading(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onDataError(e);
                    }

                    @Override
                    public void onNext(List<CompUnpaidItemModel> compUnpaidItemModels) {
                        items.addAll(compUnpaidItemModels);
                    }
                })
        );
    }

    @NonNull
    private Observable<CompUnpaidItemModel> getItemModel(@NonNull final Compensation compensation,
                                                         final int eventType,
                                                         @NonNull String identityId) {
        final String creditorId = compensation.getCreditor();
        final boolean isCredit = Objects.equals(creditorId, identityId);
        return userRepo.getIdentity(isCredit ? compensation.getDebtor() : creditorId)
                .map(new Func1<Identity, CompUnpaidItemModel>() {
                    @Override
                    public CompUnpaidItemModel call(Identity identity) {
                        return new CompUnpaidItemModel(eventType, compensation, identity,
                                moneyFormatter, isCredit);
                    }
                })
                .toObservable();
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        view.showMessage(R.string.toast_error_comps_load);
    }

    @Override
    public void onConfirmButtonClick(@NonNull CompUnpaidItemModel itemModel) {
        final BigFraction amount = itemModel.getAmountFraction();
        compConfirmingId = itemModel.getId();
        view.showCompensationAmountConfirmDialog(amount, itemModel.getNickname(), groupCurrency);
    }

    @Override
    public void onAmountConfirmed(double confirmedAmount) {
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            final CompUnpaidItemModel itemModel = items.get(i);
            if (itemModel.getViewType() != ViewType.CREDIT || !Objects.equals(itemModel.getId(), compConfirmingId)) {
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

    private void confirmCompensation(@NonNull CompUnpaidItemModel itemModel,
                                     @NonNull BigFraction amount, final boolean amountChanged) {
        getSubscriptions().add(compsRepo.confirmAmountAndAccept(itemModel.getId(), amount, amountChanged)
                .subscribe(new SingleSubscriber<Compensation>() {
                    @Override
                    public void onSuccess(Compensation compensation) {
                        view.showMessage(R.string.toast_compensation_accepted);
                        if (amountChanged) {
                            // new compensations are calculated on the server
                            setLoading(true);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        Timber.e(error, "failed to confirm compensation with error:");
                        view.showMessage(R.string.toast_error_comps_paid);
                    }
                })
        );
    }

    @Override
    public void onRemindButtonClick(@NonNull CompUnpaidItemModel itemModel) {
        final String nickname = itemModel.getNickname();
        if (itemModel.isPending()) {
            view.showMessage(R.string.toast_remind_pending, nickname);
        } else {
            compsRepo.remindDebtor(itemModel.getId());
            view.showMessage(R.string.toast_compensation_reminded_user, nickname);
        }
    }
}
