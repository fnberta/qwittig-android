/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

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
import ch.giantific.qwittig.presentation.finance.paid.itemmodels.CompsPaidItemModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link CompsPaidViewModel}.
 */
public class CompsPaidViewModelImpl extends ListViewModelBaseImpl<CompsPaidItemModel, CompsPaidViewModel.ViewListener>
        implements CompsPaidViewModel {

    private final CompensationRepository compsRepo;
    private String compGroupId;
    private NumberFormat moneyFormatter;
    private String currentGroupId;

    public CompsPaidViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull Navigator navigator,
                                  @NonNull RxBus<Object> eventBus,
                                  @NonNull UserRepository userRepo,
                                  @NonNull CompensationRepository compsRepo,
                                  @Nullable String compGroupId) {
        super(savedState, navigator, eventBus, userRepo);

        this.compsRepo = compsRepo;
        this.compGroupId = compGroupId;
    }

    @Override
    protected Class<CompsPaidItemModel> getItemModelClass() {
        return CompsPaidItemModel.class;
    }

    @Override
    protected int compareItemModels(CompsPaidItemModel o1, CompsPaidItemModel o2) {
        return o1.compareTo(o2);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(final User user) {
                        return getMatchingIdentity(user);
                    }
                })
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        moneyFormatter = MoneyUtils.getMoneyFormatter(identity.getGroupCurrency(),
                                true, true);

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

    private Observable<Identity> getMatchingIdentity(final User user) {
        return userRepo.getIdentity(user.getCurrentIdentity())
                .flatMapObservable(new Func1<Identity, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(Identity identity) {
                        if (TextUtils.isEmpty(compGroupId)
                                || Objects.equals(compGroupId, identity.getGroup())) {
                            return Observable.just(identity);
                        }

                        return userRepo.switchGroup(user, compGroupId)
                                .doOnNext(new Action1<Identity>() {
                                    @Override
                                    public void call(Identity identity) {
                                        compGroupId = identity.getGroup();
                                    }
                                })
                                .flatMap(new Func1<Identity, Observable<Identity>>() {
                                    @Override
                                    public Observable<Identity> call(Identity identity) {
                                        return Observable.never();
                                    }
                                });
                    }
                });
    }

    private void addDataListener(@NonNull final String identityId) {
        setDataListenerSub(compsRepo.observeCompensationChildren(currentGroupId, identityId, true)
                .filter(new Func1<RxChildEvent<Compensation>, Boolean>() {
                    @Override
                    public Boolean call(RxChildEvent<Compensation> compensationRxChildEvent) {
                        return initialDataLoaded;
                    }
                })
                .flatMap(new Func1<RxChildEvent<Compensation>, Observable<CompsPaidItemModel>>() {
                    @Override
                    public Observable<CompsPaidItemModel> call(final RxChildEvent<Compensation> event) {
                        return getItemModel(event.getValue(), event.getEventType(),
                                identityId);
                    }
                })
                .subscribe(this)
        );
    }

    private void loadInitialData(@NonNull final String identityId) {
        setInitialDataSub(compsRepo.getCompensations(currentGroupId, identityId, true)
                .flatMap(new Func1<Compensation, Observable<CompsPaidItemModel>>() {
                    @Override
                    public Observable<CompsPaidItemModel> call(Compensation compensation) {
                        return getItemModel(compensation, -1, identityId);
                    }
                })
                .toList()
                .subscribe(new Subscriber<List<CompsPaidItemModel>>() {
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
                    public void onNext(List<CompsPaidItemModel> compsPaidItemModels) {
                        items.addAll(compsPaidItemModels);
                    }
                })
        );
    }

    @NonNull
    private Observable<CompsPaidItemModel> getItemModel(@NonNull final Compensation compensation,
                                                        final int eventType,
                                                        @NonNull String identityId) {
        final String creditorId = compensation.getCreditor();
        final boolean isCredit = Objects.equals(creditorId, identityId);
        return userRepo.getIdentity(isCredit ? compensation.getDebtor() : creditorId)
                .map(new Func1<Identity, CompsPaidItemModel>() {
                    @Override
                    public CompsPaidItemModel call(Identity identity) {
                        return new CompsPaidItemModel(eventType, compensation, identity, isCredit,
                                moneyFormatter
                        );
                    }
                })
                .toObservable();
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        view.showMessage(R.string.toast_error_comps_load);
    }
}
