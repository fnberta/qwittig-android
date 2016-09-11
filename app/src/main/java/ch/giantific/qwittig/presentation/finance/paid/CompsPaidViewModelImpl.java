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
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.finance.paid.itemmodels.CompPaidItemModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Subscriber;

/**
 * Provides an implementation of the {@link CompsPaidViewModel}.
 */
public class CompsPaidViewModelImpl extends ListViewModelBaseImpl<CompPaidItemModel, CompsPaidViewModel.ViewListener>
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
    protected Class<CompPaidItemModel> getItemModelClass() {
        return CompPaidItemModel.class;
    }

    @Override
    protected int compareItemModels(CompPaidItemModel o1, CompPaidItemModel o2) {
        return o1.compareTo(o2);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .flatMap(this::getMatchingIdentity)
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
                .flatMapObservable(identity -> {
                    if (TextUtils.isEmpty(compGroupId)
                            || Objects.equals(compGroupId, identity.getGroup())) {
                        return Observable.just(identity);
                    }

                    return userRepo.switchGroup(user, compGroupId)
                            .doOnNext(newIdentity -> compGroupId = newIdentity.getGroup())
                            .flatMap(newIdentity -> Observable.never());
                });
    }

    private void addDataListener(@NonNull final String identityId) {
        setDataListenerSub(compsRepo.observeCompensationChildren(currentGroupId, identityId, true)
                .filter(compensationRxChildEvent -> initialDataLoaded)
                .flatMap(event -> getItemModel(event.getValue(), event.getEventType(), identityId))
                .subscribe(this)
        );
    }

    private void loadInitialData(@NonNull final String identityId) {
        setInitialDataSub(compsRepo.getCompensations(currentGroupId, identityId, true)
                .flatMap(compensation -> getItemModel(compensation, EventType.NONE, identityId))
                .toList()
                .subscribe(new Subscriber<List<CompPaidItemModel>>() {
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
                    public void onNext(List<CompPaidItemModel> compPaidItemModels) {
                        items.addAll(compPaidItemModels);
                    }
                })
        );
    }

    @NonNull
    private Observable<CompPaidItemModel> getItemModel(@NonNull final Compensation compensation,
                                                       final int eventType,
                                                       @NonNull String identityId) {
        final String creditorId = compensation.getCreditor();
        final boolean isCredit = Objects.equals(creditorId, identityId);
        return userRepo.getIdentity(isCredit ? compensation.getDebtor() : creditorId)
                .map(identity -> new CompPaidItemModel(eventType, compensation, identity, isCredit, moneyFormatter))
                .toObservable();
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        view.showMessage(R.string.toast_error_comps_load);
    }
}
