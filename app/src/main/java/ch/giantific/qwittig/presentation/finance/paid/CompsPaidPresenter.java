/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.CompensationRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.CompsPaidViewModel;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.items.CompPaidItemViewViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link CompsPaidContract}.
 */
public class CompsPaidPresenter extends BasePresenterImpl<CompsPaidContract.ViewListener>
        implements CompsPaidContract.Presenter {

    private static final String STATE_VIEW_MODEL = CompsPaidViewModel.class.getCanonicalName();
    private final CompsPaidViewModel viewModel;
    private final SortedList<CompPaidItemViewViewModel> items;
    private final SortedListCallback<CompPaidItemViewViewModel> listCallback;
    private final CompensationRepository compsRepo;
    private boolean initialDataLoaded;
    private String compGroupId;
    private NumberFormat moneyFormatter;
    private String currentGroupId;

    public CompsPaidPresenter(@Nullable Bundle savedState,
                              @NonNull Navigator navigator,
                              @NonNull UserRepository userRepo,
                              @NonNull CompensationRepository compsRepo,
                              @Nullable String compGroupId) {
        super(savedState, navigator, userRepo);

        this.compsRepo = compsRepo;
        this.compGroupId = compGroupId;

        listCallback = new SortedListCallback<CompPaidItemViewViewModel>() {
            @Override
            public int compare(CompPaidItemViewViewModel o1, CompPaidItemViewViewModel o2) {
                return o1.compareTo(o2);
            }
        };
        items = new SortedList<>(CompPaidItemViewViewModel.class, listCallback);

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
        } else {
            viewModel = new CompsPaidViewModel(true);
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
    }

    @Override
    public CompsPaidViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        listCallback.setListInteraction(listInteraction);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
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
                        addDataListener(identityId, groupId);
                        loadInitialData(identityId, groupId);
                    }
                })
        );
    }

    private Observable<Identity> getMatchingIdentity(@NonNull String currentIdentityId) {
        return userRepo.getIdentity(currentIdentityId)
                .flatMapObservable(identity -> {
                    if (TextUtils.isEmpty(compGroupId)
                            || Objects.equals(compGroupId, identity.getGroup())) {
                        return Observable.just(identity);
                    }

                    // switch group and return Observable.never(), change in currentIdentity will
                    // trigger chain to start again from the top
                    return userRepo.switchGroup(currentIdentityId, compGroupId)
                            .doOnNext(newIdentity -> compGroupId = newIdentity.getGroup())
                            .flatMap(newIdentity -> Observable.never());
                });
    }

    private void addDataListener(@NonNull final String identityId, @NonNull String groupId) {
        subscriptions.add(compsRepo.observeCompensationChildren(groupId, identityId, true)
                .filter(compensationRxChildEvent -> initialDataLoaded)
                .takeWhile(childEvent -> Objects.equals(childEvent.getValue().getGroup(), currentGroupId))
                .flatMap(event -> getItemViewModel(event.getValue(), event.getEventType(), identityId))
                .subscribe(new ChildEventSubscriber<>(items, viewModel, e ->
                        view.showMessage(R.string.toast_error_comps_load)))
        );
    }

    private void loadInitialData(@NonNull final String identityId, @NonNull String groupId) {
        subscriptions.add(compsRepo.getCompensations(groupId, identityId, true)
                .takeWhile(compensation -> Objects.equals(compensation.getGroup(), currentGroupId))
                .flatMap(compensation -> getItemViewModel(compensation, EventType.NONE, identityId))
                .toList()
                .subscribe(new Subscriber<List<CompPaidItemViewViewModel>>() {
                    @Override
                    public void onCompleted() {
                        initialDataLoaded = true;
                        viewModel.setEmpty(getItemCount() == 0);

                        viewModel.setLoading(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "failed to load initial paid compensations with error:");
                        view.showMessage(R.string.toast_error_comps_load);
                    }

                    @Override
                    public void onNext(List<CompPaidItemViewViewModel> compPaidItemViewModels) {
                        items.addAll(compPaidItemViewModels);
                    }
                })
        );
    }

    @NonNull
    private Observable<CompPaidItemViewViewModel> getItemViewModel(@NonNull final Compensation compensation,
                                                                   final int eventType,
                                                                   @NonNull String identityId) {
        final String creditorId = compensation.getCreditor();
        final boolean isCredit = Objects.equals(creditorId, identityId);
        return userRepo.getIdentity(isCredit ? compensation.getDebtor() : creditorId)
                .map(identity -> new CompPaidItemViewViewModel(eventType, compensation, identity, isCredit, moneyFormatter))
                .toObservable();
    }

    @Override
    public CompPaidItemViewViewModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
