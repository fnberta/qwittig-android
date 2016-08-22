/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.details;

import android.databinding.Bindable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ch.giantific.qwittig.BR;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.data.repositories.PurchaseRepository;
import ch.giantific.qwittig.data.repositories.UserRepository;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.IndefiniteSubscriber;
import ch.giantific.qwittig.presentation.common.ListInteraction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModelBaseImpl;
import ch.giantific.qwittig.presentation.purchases.details.itemmodels.PurchaseDetailsIdentityItemModel;
import ch.giantific.qwittig.presentation.purchases.details.itemmodels.PurchaseDetailsItemModel;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link PurchaseDetailsViewModel}.
 */
public class PurchaseDetailsViewModelImpl extends ViewModelBaseImpl<PurchaseDetailsViewModel.ViewListener>
        implements PurchaseDetailsViewModel {

    private final PurchaseRepository purchaseRepo;
    private final String purchaseId;
    private final String purchaseGroupId;
    private final List<PurchaseDetailsItemModel> items;
    private final List<PurchaseDetailsIdentityItemModel> identityItems;
    private final DateFormat dateFormatter;
    private ListInteraction itemsListInteraction;
    private ListInteraction identitiesListInteraction;
    private String currentIdentityId;
    private NumberFormat moneyFormatter;
    private NumberFormat foreignMoneyFormatter;
    private String store;
    private String date;
    private String receipt;
    private String total;
    private String totalForeign;
    private String myShare;
    private String myShareForeign;
    private String note;
    private double exchangeRate;
    private boolean identitiesActive = true;

    public PurchaseDetailsViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepo,
                                        @NonNull PurchaseRepository purchaseRepo,
                                        @NonNull String purchaseId,
                                        @Nullable String purchaseGroupId) {
        super(savedState, navigator, eventBus, userRepo);

        this.purchaseRepo = purchaseRepo;
        this.purchaseId = purchaseId;
        this.purchaseGroupId = purchaseGroupId;
        items = new ArrayList<>();
        identityItems = new ArrayList<>();
        dateFormatter = DateUtils.getDateFormatter(false);
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        itemsListInteraction = listInteraction;
    }

    @Override
    public void setIdentitiesListInteraction(@NonNull ListInteraction listInteraction) {
        identitiesListInteraction = listInteraction;
    }

    @Override
    @Bindable
    public String getStore() {
        return store;
    }

    @Override
    public void setStore(@NonNull String store) {
        this.store = store;
        notifyPropertyChanged(BR.store);
    }

    @Override
    @Bindable
    public String getDate() {
        return date;
    }

    @Override
    public void setDate(@NonNull Date date) {
        this.date = dateFormatter.format(date);
        notifyPropertyChanged(BR.date);
    }

    @Override
    @Bindable
    public String getTotal() {
        return total;
    }

    @Override
    public void setTotal(double total) {
        this.total = moneyFormatter.format(total);
        notifyPropertyChanged(BR.total);
    }

    @Override
    @Bindable
    public String getTotalForeign() {
        return totalForeign;
    }

    @Override
    public void setTotalForeign(double totalForeign) {
        this.totalForeign = foreignMoneyFormatter.format(totalForeign);
        notifyPropertyChanged(BR.totalForeign);
    }

    @Override
    @Bindable
    public String getMyShare() {
        return myShare;
    }

    @Override
    public void setMyShare(double myShare) {
        this.myShare = moneyFormatter.format(myShare);
        notifyPropertyChanged(BR.myShare);
    }

    @Override
    @Bindable
    public String getMyShareForeign() {
        return myShareForeign;
    }

    @Override
    public void setMyShareForeign(double myShareForeign) {
        this.myShareForeign = foreignMoneyFormatter.format(myShareForeign);
        notifyPropertyChanged(BR.myShareForeign);
    }

    @Override
    @Bindable
    public String getNote() {
        return note;
    }

    @Override
    public void setNote(@NonNull String note) {
        this.note = note;
        notifyPropertyChanged(BR.note);
        notifyPropertyChanged(BR.noteAvailable);
    }

    @Override
    @Bindable
    public boolean isNoteAvailable() {
        return !TextUtils.isEmpty(note);
    }

    @Override
    @Bindable
    public String getReceipt() {
        return receipt;
    }

    @Override
    public void setReceipt(@NonNull String receiptUrl) {
        receipt = receiptUrl;
        notifyPropertyChanged(BR.receipt);
        notifyPropertyChanged(BR.receiptAvailable);
    }

    @Override
    @Bindable
    public boolean isReceiptAvailable() {
        return !TextUtils.isEmpty(receipt);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(userRepo.observeUser(currentUser.getUid())
                .doOnNext(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        final String currentIdentityId = user.getCurrentIdentity();
                        if (!TextUtils.isEmpty(PurchaseDetailsViewModelImpl.this.currentIdentityId)
                                && !Objects.equals(PurchaseDetailsViewModelImpl.this.currentIdentityId, currentIdentityId)) {
                            navigator.finish();
                        }

                        PurchaseDetailsViewModelImpl.this.currentIdentityId = currentIdentityId;
                    }
                })
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(final User user) {
                        return userRepo.getIdentity(user.getCurrentIdentity()).toObservable()
                                .flatMap(new Func1<Identity, Observable<Identity>>() {
                                    @Override
                                    public Observable<Identity> call(final Identity identity) {
                                        if (TextUtils.isEmpty(purchaseGroupId)
                                                || Objects.equals(purchaseGroupId, identity.getGroup())) {
                                            return Observable.just(identity);
                                        }

                                        return userRepo.switchGroup(user, purchaseGroupId)
                                                .flatMap(new Func1<Identity, Observable<Identity>>() {
                                                    @Override
                                                    public Observable<Identity> call(Identity identity) {
                                                        return Observable.never();
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .doOnNext(new Action1<Identity>() {
                    @Override
                    public void call(Identity identity) {
                        final String currency = identity.getGroupCurrency();
                        moneyFormatter = MoneyUtils.getMoneyFormatter(currency, true, true);
                    }
                })
                .flatMap(new Func1<Identity, Observable<Purchase>>() {
                    @Override
                    public Observable<Purchase> call(final Identity identity) {
                        return purchaseRepo.observePurchase(purchaseId)
                                .doOnNext(new Action1<Purchase>() {
                                    @Override
                                    public void call(Purchase purchase) {
                                        foreignMoneyFormatter = MoneyUtils.getMoneyFormatter(purchase.getCurrency(), true, true);
                                        final String currentIdentityId = identity.getId();
                                        updateReadBy(purchase, currentIdentityId);
                                        updateActionBarMenu(purchase, identity.getGroupCurrency(), currentIdentityId);
                                        setPurchaseDetails(purchase, currentIdentityId);
                                    }
                                });
                    }
                })
                .flatMap(new Func1<Purchase, Observable<List<PurchaseDetailsIdentityItemModel>>>() {
                    @Override
                    public Observable<List<PurchaseDetailsIdentityItemModel>> call(final Purchase purchase) {
                        return Observable.from(purchase.getIdentitiesIds())
                                .flatMap(new Func1<String, Observable<Identity>>() {
                                    @Override
                                    public Observable<Identity> call(String identityId) {
                                        return userRepo.getIdentity(identityId).toObservable();
                                    }
                                })
                                .map(new Func1<Identity, PurchaseDetailsIdentityItemModel>() {
                                    @Override
                                    public PurchaseDetailsIdentityItemModel call(Identity identity) {
                                        final boolean isBuyer = Objects.equals(purchase.getBuyer(), identity.getId());
                                        return new PurchaseDetailsIdentityItemModel(identity, isBuyer);
                                    }
                                })
                                .toList();
                    }
                })
                .doOnNext(new Action1<List<PurchaseDetailsIdentityItemModel>>() {
                    @Override
                    public void call(List<PurchaseDetailsIdentityItemModel> itemModels) {
                        identityItems.clear();
                        identityItems.addAll(itemModels);
                        identitiesListInteraction.notifyDataSetChanged();

                        checkIdentitiesActive(itemModels);
                    }
                })
                .subscribe(new IndefiniteSubscriber<List<PurchaseDetailsIdentityItemModel>>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        view.showMessage(R.string.toast_error_purchase_details_load);
                    }

                    @Override
                    public void onNext(List<PurchaseDetailsIdentityItemModel> itemModels) {
                        setLoading(false);
                        view.startEnterTransition();
                    }
                })
        );
    }

    private void setPurchaseDetails(@NonNull Purchase purchase, @NonNull String identityId) {
        setStore(purchase.getStore());
        setDate(purchase.getDateDate());
        setTotal(purchase.getTotal());
        setTotalForeign(purchase.getTotalForeign());
        final double myShare = purchase.calculateUserShare(identityId);
        setMyShare(myShare);
        exchangeRate = purchase.getExchangeRate();
        setMyShareForeign(myShare / exchangeRate);
        setNote(purchase.getNote());
        setReceipt(purchase.getReceipt());
        setItems(purchase.getItems(), identityId);
    }

    private void setItems(@NonNull List<Item> items, @NonNull String identityId) {
        this.items.clear();
        for (Item item : items) {
            final PurchaseDetailsItemModel itemModel =
                    new PurchaseDetailsItemModel(item, identityId, moneyFormatter);
            this.items.add(itemModel);
        }
        itemsListInteraction.notifyDataSetChanged();
    }

    private void checkIdentitiesActive(@NonNull List<PurchaseDetailsIdentityItemModel> itemModels) {
        for (PurchaseDetailsIdentityItemModel itemModel : itemModels) {
            if (!itemModel.isActive()) {
                identitiesActive = false;
                break;
            }
        }
    }

    private void updateActionBarMenu(@NonNull Purchase purchase,
                                     @NonNull String groupCurrency,
                                     @NonNull String currentIdentity) {
        final boolean showEdit = Objects.equals(purchase.getBuyer(), currentIdentity);
        final boolean showExchangeRate = !Objects.equals(groupCurrency, purchase.getCurrency());
        view.toggleMenuOptions(showEdit, showExchangeRate);
    }

    private void updateReadBy(@NonNull Purchase purchase, @NonNull String currentIdentity) {
        if (!purchase.isRead(currentIdentity)) {
            purchaseRepo.updateReadBy(purchaseId, currentIdentity);
        }
    }

    @Override
    public void onEditPurchaseClick() {
        if (identitiesActive) {
            navigator.startPurchaseEdit(purchaseId, false);
        } else {
            view.showMessage(R.string.toast_purchase_edit_identities_inactive);
        }
    }

    @Override
    public void onDeletePurchaseClick() {
        if (identitiesActive) {
            purchaseRepo.deletePurchase(purchaseId);
            navigator.finish(PurchaseDetailsResult.PURCHASE_DELETED, purchaseId);
        } else {
            view.showMessage(R.string.toast_purchase_edit_identities_inactive);
        }
    }

    @Override
    public void onShowExchangeRateClick() {
        final NumberFormat formatter = MoneyUtils.getExchangeRateFormatter();
        view.showMessage(R.string.toast_exchange_rate_value, formatter.format(exchangeRate));
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public PurchaseDetailsItemModel getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        throw new RuntimeException("only one view type supported in this view!");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public PurchaseDetailsIdentityItemModel getIdentityAtPosition(int position) {
        return identityItems.get(position);
    }

    @Override
    public int getIdentitiesCount() {
        return identityItems.size();
    }
}
