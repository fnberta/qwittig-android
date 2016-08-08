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
import ch.giantific.qwittig.data.rxwrapper.firebase.RxChildEvent;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ListViewModelBaseImpl;
import ch.giantific.qwittig.presentation.purchases.list.purchases.itemmodels.PurchasesItemModel;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link PurchasesViewModel}.
 */
public class PurchasesViewModelImpl extends ListViewModelBaseImpl<PurchasesItemModel, PurchasesViewModel.ViewListener>
        implements PurchasesViewModel {

    private final PurchaseRepository mPurchaseRepo;
    private NumberFormat mMoneyFormatter;
    private DateFormat mDateFormatter;
    private String mCurrentGroupId;

    public PurchasesViewModelImpl(@Nullable Bundle savedState,
                                  @NonNull Navigator navigator,
                                  @NonNull RxBus<Object> eventBus,
                                  @NonNull UserRepository userRepository,
                                  @NonNull PurchaseRepository purchaseRepo) {
        super(savedState, navigator, eventBus, userRepository);

        mPurchaseRepo = purchaseRepo;
    }

    @Override
    protected Class<PurchasesItemModel> getItemModelClass() {
        return PurchasesItemModel.class;
    }

    @Override
    protected int compareItemModels(PurchasesItemModel o1, PurchasesItemModel o2) {
        return o1.compareTo(o2);
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
                        final String currency = identity.getGroupCurrency();
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, false, true);
                        mDateFormatter = DateUtils.getDateFormatter(true);

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
        setDataListenerSub(mPurchaseRepo.observePurchaseChildren(mCurrentGroupId, identityId, false)
                .filter(new Func1<RxChildEvent<Purchase>, Boolean>() {
                    @Override
                    public Boolean call(RxChildEvent<Purchase> purchaseRxChildEvent) {
                        return mInitialDataLoaded;
                    }
                })
                .flatMap(new Func1<RxChildEvent<Purchase>, Observable<PurchasesItemModel>>() {
                    @Override
                    public Observable<PurchasesItemModel> call(final RxChildEvent<Purchase> event) {
                        return getItemModels(event.getValue(), event.getEventType(), identityId);
                    }
                })
                .subscribe(this)
        );
    }

    private void loadInitialData(@NonNull final String identityId) {
        setInitialDataSub(mPurchaseRepo.getPurchases(mCurrentGroupId, identityId, false)
                .flatMap(new Func1<Purchase, Observable<PurchasesItemModel>>() {
                    @Override
                    public Observable<PurchasesItemModel> call(final Purchase purchase) {
                        return getItemModels(purchase, -1, identityId);
                    }
                })
                .toList()
                .subscribe(new Subscriber<List<PurchasesItemModel>>() {
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
                    public void onNext(List<PurchasesItemModel> purchasesItemModels) {
                        mItems.addAll(purchasesItemModels);
                    }
                })
        );
    }

    @NonNull
    private Observable<PurchasesItemModel> getItemModels(@NonNull final Purchase purchase,
                                                         final int eventType,
                                                         @NonNull final String currentIdentityId) {
        return mUserRepo.getIdentity(purchase.getBuyer())
                .map(new Func1<Identity, PurchasesItemModel>() {
                    @Override
                    public PurchasesItemModel call(Identity buyer) {
                        return new PurchasesItemModel(eventType, purchase, buyer, currentIdentityId,
                                mMoneyFormatter, mDateFormatter);
                    }
                })
                .toObservable();
    }

    @Override
    protected void onDataError(@NonNull Throwable e) {
        super.onDataError(e);

        mView.showMessage(R.string.toast_error_purchases_load);
    }

    @Override
    public void onPurchaseRowItemClick(@NonNull PurchasesItemModel itemModel) {
        mNavigator.startPurchaseDetails(itemModel.getId());
    }

    @Override
    public void onPurchaseDeleted(@NonNull String purchaseId) {
        mView.showMessage(R.string.toast_purchase_deleted);
        mItems.removeItemAt(getPositionForId(purchaseId));
        notifyPropertyChanged(BR.empty);
    }
}
