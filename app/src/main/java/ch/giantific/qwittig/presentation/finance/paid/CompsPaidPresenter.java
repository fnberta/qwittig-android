/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.paid;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.CompensationRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.CompsPaidViewModel;
import ch.giantific.qwittig.presentation.finance.paid.viewmodels.items.CompPaidItemViewModel;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import rx.Observable;

/**
 * Provides an implementation of the {@link CompsPaidContract}.
 */
public class CompsPaidPresenter extends BasePresenterImpl<CompsPaidContract.ViewListener>
        implements CompsPaidContract.Presenter {

    private final CompsPaidViewModel viewModel;
    private final CompensationRepository compsRepo;
    private String compGroupId;
    private NumberFormat moneyFormatter;
    private String currentGroupId;

    public CompsPaidPresenter(@NonNull Navigator navigator,
                              @NonNull CompsPaidViewModel viewModel,
                              @NonNull UserRepository userRepo,
                              @NonNull CompensationRepository compsRepo,
                              @Nullable String compGroupId) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.compsRepo = compsRepo;
        this.compGroupId = compGroupId;
    }

    @Override
    public int compareItemViewModels(@NonNull CompPaidItemViewModel item1,
                                     @NonNull CompPaidItemViewModel item2) {
        return item1.compareTo(item2);
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

                        final String identityId = identity.getId();
                        final String groupId = identity.getGroup();
                        if (!Objects.equals(currentGroupId, groupId)) {
                            view.clearItems();
                        }
                        currentGroupId = groupId;
                        addDataListener(identityId, groupId);
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
        final Observable<List<CompPaidItemViewModel>> initialData = compsRepo.getCompensations(groupId, identityId, true)
                .flatMap(compensation -> getItemViewModel(compensation, EventType.NONE, identityId))
                .toList()
                .doOnNext(items -> {
                    view.addItems(items);
                    viewModel.setEmpty(view.isItemsEmpty());
                    viewModel.setLoading(false);
                });
        subscriptions.add(compsRepo.observeCompensationChildren(groupId, identityId, true)
                .skipUntil(initialData)
                .takeWhile(childEvent -> Objects.equals(childEvent.getValue().getGroup(), currentGroupId))
                .flatMap(event -> getItemViewModel(event.getValue(), event.getEventType(), identityId))
                .subscribe(new ChildEventSubscriber<>(view, viewModel, e ->
                        view.showMessage(R.string.toast_error_comps_load)))
        );
    }

    @NonNull
    private Observable<CompPaidItemViewModel> getItemViewModel(@NonNull final Compensation compensation,
                                                               final int eventType,
                                                               @NonNull String identityId) {
        final String creditorId = compensation.getCreditor();
        final boolean isCredit = Objects.equals(creditorId, identityId);
        return userRepo.getIdentity(isCredit ? compensation.getDebtor() : creditorId)
                .map(identity -> new CompPaidItemViewModel(eventType, compensation, identity, isCredit, moneyFormatter))
                .toObservable();
    }
}
