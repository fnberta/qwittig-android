/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;

import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.listadapters.SortedListCallback;
import ch.giantific.qwittig.presentation.common.listadapters.interactions.ListInteraction;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.PurchasesViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.items.PurchaseItemViewModel;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Provides an implementation of the {@link PurchasesContract}.
 */
public class PurchasesPresenter extends BasePresenterImpl<PurchasesContract.ViewListener>
        implements PurchasesContract.Presenter {

    private static final String STATE_VIEW_MODEL = PurchasesViewModel.class.getCanonicalName();
    private final PurchasesViewModel viewModel;
    private final SortedList<PurchaseItemViewModel> items;
    private final SortedListCallback<PurchaseItemViewModel> listCallback;
    private final ChildEventSubscriber<PurchaseItemViewModel, PurchasesViewModel> subscriber;
    private final PurchaseRepository purchaseRepo;
    private boolean initialDataLoaded;
    private NumberFormat moneyFormatter;
    private DateFormat dateFormatter;
    private String currentGroupId;

    public PurchasesPresenter(@Nullable Bundle savedState,
                              @NonNull Navigator navigator,
                              @NonNull UserRepository userRepo,
                              @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, navigator, userRepo);

        this.purchaseRepo = purchaseRepo;

        listCallback = new SortedListCallback<PurchaseItemViewModel>() {
            @Override
            public int compare(PurchaseItemViewModel o1, PurchaseItemViewModel o2) {
                return o1.compareTo(o2);
            }
        };
        items = new SortedList<>(PurchaseItemViewModel.class, listCallback);

        if (savedState != null) {
            viewModel = savedState.getParcelable(STATE_VIEW_MODEL);
        } else {
            viewModel = new PurchasesViewModel(true);
        }

        //noinspection ConstantConditions
        subscriber = new ChildEventSubscriber<>(items, viewModel, e ->
                view.showMessage(R.string.toast_error_purchases_load));
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putParcelable(STATE_VIEW_MODEL, viewModel);
    }

    @Override
    public PurchasesViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        listCallback.setListInteraction(listInteraction);
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        subscriptions.add(userRepo.observeCurrentIdentityId(currentUser.getUid())
                .flatMap(currentIdentityId -> userRepo.getIdentity(currentIdentityId).toObservable())
                .subscribe(new IndefiniteSubscriber<Identity>() {
                    @Override
                    public void onNext(Identity identity) {
                        final String currency = identity.getGroupCurrency();
                        moneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);
                        dateFormatter = DateUtils.getDateFormatter(true);

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

    private void addDataListener(@NonNull final String identityId, @NonNull String groupId) {
        subscriptions.add(purchaseRepo.observePurchaseChildren(groupId, identityId, false)
                .filter(purchaseRxChildEvent -> initialDataLoaded)
                .takeWhile(purchaseRxChildEvent ->
                        Objects.equals(purchaseRxChildEvent.getValue().getGroup(), currentGroupId))
                .flatMap(event -> getItemViewModel(event.getValue(), event.getEventType(), identityId))
                .subscribe(subscriber)
        );
    }

    private void loadInitialData(@NonNull final String identityId, @NonNull String groupId) {
        subscriptions.add(purchaseRepo.getPurchases(groupId, identityId, false)
                .takeWhile(purchase -> Objects.equals(purchase.getGroup(), currentGroupId))
                .flatMap(purchase -> getItemViewModel(purchase, EventType.NONE, identityId))
                .toList()
                .subscribe(new Subscriber<List<PurchaseItemViewModel>>() {
                    @Override
                    public void onCompleted() {
                        initialDataLoaded = true;
                        viewModel.setEmpty(getItemCount() == 0);
                        viewModel.setLoading(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "failed to load initial purchases with error:");
                        view.showMessage(R.string.toast_error_purchases_load);
                    }

                    @Override
                    public void onNext(List<PurchaseItemViewModel> purchaseItemModels) {
                        items.addAll(purchaseItemModels);
                    }
                })
        );
    }

    @NonNull
    private Observable<PurchaseItemViewModel> getItemViewModel(@NonNull final Purchase purchase,
                                                               final int eventType,
                                                               @NonNull final String currentIdentityId) {
        return userRepo.getIdentity(purchase.getBuyer())
                .map(buyer -> new PurchaseItemViewModel(eventType, purchase, buyer, currentIdentityId,
                        moneyFormatter, dateFormatter))
                .toObservable();
    }

    @Override
    public PurchaseItemViewModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onPurchaseRowItemClick(@NonNull PurchaseItemViewModel itemViewModel) {
        navigator.startPurchaseDetails(itemViewModel.getId());
    }

    @Override
    public void onPurchaseDeleted(@NonNull String purchaseId) {
        view.showMessage(R.string.toast_purchase_deleted);
        items.removeItemAt(subscriber.getPositionForId(purchaseId));
        viewModel.setEmpty(getItemCount() == 0);
    }
}
