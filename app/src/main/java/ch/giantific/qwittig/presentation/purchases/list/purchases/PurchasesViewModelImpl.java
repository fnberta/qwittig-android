/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.list.purchases;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent.EventType;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.purchases.list.purchases.itemmodels.PurchaseItemModel;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Subscriber;

/**
 * Provides an implementation of the {@link PurchasesViewModel}.
 */
public class PurchasesViewModelImpl extends ListViewModelBaseImpl<PurchaseItemModel, PurchasesViewModel.ViewListener>
        implements PurchasesViewModel {

    private final PurchaseRepository purchaseRepo;
    private NumberFormat moneyFormatter;
    private DateFormat dateFormatter;
    private String currentGroupId;

    public PurchasesViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull Navigator navigator,
                                  @NonNull RxBus<Object> eventBus,
                                  @NonNull UserRepository userRepo,
                                  @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, navigator, eventBus, userRepo);

        this.purchaseRepo = purchaseRepo;
    }

    @Override
    protected Class<PurchaseItemModel> getItemModelClass() {
        return PurchaseItemModel.class;
    }

    @Override
    protected int compareItemModels(PurchaseItemModel o1, PurchaseItemModel o2) {
        return o1.compareTo(o2);
    }

    @Override
    protected void onUserLoggedIn(@NonNull final FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeCurrentIdentityId(currentUser.getUid())
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
        getSubscriptions().add(purchaseRepo.observePurchaseChildren(groupId, identityId, false)
                .filter(purchaseRxChildEvent -> initialDataLoaded)
                .takeWhile(purchaseRxChildEvent ->
                        Objects.equals(purchaseRxChildEvent.getValue().getGroup(), currentGroupId))
                .flatMap(event -> getItemModel(event.getValue(), event.getEventType(), identityId))
                .subscribe(this)
        );
    }

    private void loadInitialData(@NonNull final String identityId, @NonNull String groupId) {
        getSubscriptions().add(purchaseRepo.getPurchases(groupId, identityId, false)
                .takeWhile(purchase -> Objects.equals(purchase.getGroup(), currentGroupId))
                .flatMap(purchase -> getItemModel(purchase, EventType.NONE, identityId))
                .toList()
                .subscribe(new Subscriber<List<PurchaseItemModel>>() {
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
                    public void onNext(List<PurchaseItemModel> purchaseItemModels) {
                        items.addAll(purchaseItemModels);
                    }
                })
        );
    }

    @NonNull
    private Observable<PurchaseItemModel> getItemModel(@NonNull final Purchase purchase,
                                                       final int eventType,
                                                       @NonNull final String currentIdentityId) {
        return userRepo.getIdentity(purchase.getBuyer())
                .map(buyer -> new PurchaseItemModel(eventType, purchase, buyer, currentIdentityId,
                        moneyFormatter, dateFormatter))
                .toObservable();
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        view.showMessage(R.string.toast_error_purchases_load);
    }

    @Override
    public void onPurchaseRowItemClick(@NonNull PurchaseItemModel itemModel) {
        navigator.startPurchaseDetails(itemModel.getId());
    }

    @Override
    public void onPurchaseDeleted(@NonNull String purchaseId) {
        view.showMessage(R.string.toast_purchase_deleted);
        items.removeItemAt(getPositionForId(purchaseId));
        notifyPropertyChanged(BR.empty);
    }
}
