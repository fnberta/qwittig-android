/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenterImpl;
import ch.giantific.qwittig.presentation.common.subscribers.ChildEventSubscriber;
import ch.giantific.qwittig.presentation.common.subscribers.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.PurchasesViewModel;
import ch.giantific.qwittig.presentation.purchases.list.purchases.viewmodels.items.PurchaseItemViewModel;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.rxwrapper.firebase.RxChildEvent.EventType;
import rx.Observable;

/**
 * Provides an implementation of the {@link PurchasesContract}.
 */
public class PurchasesPresenter extends BasePresenterImpl<PurchasesContract.ViewListener>
        implements PurchasesContract.Presenter {

    private final PurchasesViewModel viewModel;
    private final PurchaseRepository purchaseRepo;
    private NumberFormat moneyFormatter;
    private DateFormat dateFormatter;
    private String currentGroupId;

    @Inject
    public PurchasesPresenter(@NonNull Navigator navigator,
                              @NonNull PurchasesViewModel viewModel,
                              @NonNull UserRepository userRepo,
                              @NonNull PurchaseRepository purchaseRepo) {
        super(navigator, userRepo);

        this.viewModel = viewModel;
        this.purchaseRepo = purchaseRepo;
    }

    @Override
    public int compareItemViewModels(@NonNull PurchaseItemViewModel item1, @NonNull PurchaseItemViewModel item2) {
        return item1.compareTo(item2);
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

    private void addDataListener(@NonNull final String identityId, @NonNull String groupId) {
        final Observable<List<PurchaseItemViewModel>> initialData = purchaseRepo.getPurchases(groupId, identityId, false)
                .flatMap(purchase -> getItemViewModel(purchase, EventType.NONE, identityId))
                .toList()
                .doOnNext(purchaseItemViewModels -> {
                    view.addItems(purchaseItemViewModels);
                    viewModel.setEmpty(view.isItemsEmpty());
                    viewModel.setLoading(false);
                });
        subscriptions.add(purchaseRepo.observePurchaseChildren(groupId, identityId, false)
                .skipUntil(initialData)
                .takeWhile(purchaseRxChildEvent ->
                        Objects.equals(purchaseRxChildEvent.getValue().getGroup(), currentGroupId))
                .flatMap(event -> getItemViewModel(event.getValue(), event.getEventType(), identityId))
                .subscribe(new ChildEventSubscriber<>(view, viewModel, e ->
                        view.showMessage(R.string.toast_error_purchases_load)))
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
    public void onPurchaseRowItemClick(@NonNull PurchaseItemViewModel itemViewModel) {
        navigator.startPurchaseDetails(itemViewModel.getId());
    }

    @Override
    public void onPurchaseDeleted(@NonNull String purchaseId) {
        view.showMessage(R.string.toast_purchase_deleted);
        view.removeItemAtPosition(view.getItemPositionForId(purchaseId));
        viewModel.setEmpty(view.isItemsEmpty());
    }
}
