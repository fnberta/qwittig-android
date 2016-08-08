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

    private final PurchaseRepository mPurchaseRepo;
    private final String mPurchaseId;
    private final String mPurchaseGroupId;
    private final List<PurchaseDetailsItemModel> mItems;
    private final List<PurchaseDetailsIdentityItemModel> mIdentityItems;
    private final DateFormat mDateFormatter;
    private ListInteraction mItemsListInteraction;
    private ListInteraction mIdentitiesListInteraction;
    private String mCurrentIdentityId;
    private NumberFormat mMoneyFormatter;
    private NumberFormat mForeignMoneyFormatter;
    private String mStore;
    private String mDate;
    private String mReceipt;
    private String mTotal;
    private String mTotalForeign;
    private String mMyShare;
    private String mMyShareForeign;
    private String mNote;
    private double mExchangeRate;
    private boolean mIdentitiesActive = true;

    public PurchaseDetailsViewModelImpl(@Nullable Bundle savedState,
                                        @NonNull Navigator navigator,
                                        @NonNull RxBus<Object> eventBus,
                                        @NonNull UserRepository userRepository,
                                        @NonNull PurchaseRepository purchaseRepo,
                                        @NonNull String purchaseId,
                                        @Nullable String purchaseGroupId) {
        super(savedState, navigator, eventBus, userRepository);

        mPurchaseRepo = purchaseRepo;
        mPurchaseId = purchaseId;
        mPurchaseGroupId = purchaseGroupId;
        mItems = new ArrayList<>();
        mIdentityItems = new ArrayList<>();
        mDateFormatter = DateUtils.getDateFormatter(false);
    }

    @Override
    public void setListInteraction(@NonNull ListInteraction listInteraction) {
        mItemsListInteraction = listInteraction;
    }

    @Override
    public void setIdentitiesListInteraction(@NonNull ListInteraction listInteraction) {
        mIdentitiesListInteraction = listInteraction;
    }

    @Override
    @Bindable
    public String getStore() {
        return mStore;
    }

    @Override
    public void setStore(@NonNull String store) {
        mStore = store;
        notifyPropertyChanged(BR.store);
    }

    @Override
    @Bindable
    public String getDate() {
        return mDate;
    }

    @Override
    public void setDate(@NonNull Date date) {
        mDate = mDateFormatter.format(date);
        notifyPropertyChanged(BR.date);
    }

    @Override
    @Bindable
    public String getTotal() {
        return mTotal;
    }

    @Override
    public void setTotal(double total) {
        mTotal = mMoneyFormatter.format(total);
        notifyPropertyChanged(BR.total);
    }

    @Override
    @Bindable
    public String getTotalForeign() {
        return mTotalForeign;
    }

    @Override
    public void setTotalForeign(double totalForeign) {
        mTotalForeign = mForeignMoneyFormatter.format(totalForeign);
        notifyPropertyChanged(BR.totalForeign);
    }

    @Override
    @Bindable
    public String getMyShare() {
        return mMyShare;
    }

    @Override
    public void setMyShare(double myShare) {
        mMyShare = mMoneyFormatter.format(myShare);
        notifyPropertyChanged(BR.myShare);
    }

    @Override
    @Bindable
    public String getMyShareForeign() {
        return mMyShareForeign;
    }

    @Override
    public void setMyShareForeign(double myShareForeign) {
        mMyShareForeign = mForeignMoneyFormatter.format(myShareForeign);
        notifyPropertyChanged(BR.myShareForeign);
    }

    @Override
    @Bindable
    public String getNote() {
        return mNote;
    }

    @Override
    public void setNote(@NonNull String note) {
        mNote = note;
        notifyPropertyChanged(BR.note);
        notifyPropertyChanged(BR.noteAvailable);
    }

    @Override
    @Bindable
    public boolean isNoteAvailable() {
        return !TextUtils.isEmpty(mNote);
    }

    @Override
    @Bindable
    public String getReceipt() {
        return mReceipt;
    }

    @Override
    public void setReceipt(@NonNull String receiptUrl) {
        mReceipt = receiptUrl;
        notifyPropertyChanged(BR.receipt);
        notifyPropertyChanged(BR.receiptAvailable);
    }

    @Override
    @Bindable
    public boolean isReceiptAvailable() {
        return !TextUtils.isEmpty(mReceipt);
    }

    @Override
    protected void onUserLoggedIn(@NonNull FirebaseUser currentUser) {
        super.onUserLoggedIn(currentUser);

        getSubscriptions().add(mUserRepo.observeUser(currentUser.getUid())
                .doOnNext(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        final String currentIdentityId = user.getCurrentIdentity();
                        if (!TextUtils.isEmpty(mCurrentIdentityId)
                                && !Objects.equals(mCurrentIdentityId, currentIdentityId)) {
                            mNavigator.finish();
                        }

                        mCurrentIdentityId = currentIdentityId;
                    }
                })
                .flatMap(new Func1<User, Observable<Identity>>() {
                    @Override
                    public Observable<Identity> call(final User user) {
                        return mUserRepo.getIdentity(user.getCurrentIdentity()).toObservable()
                                .flatMap(new Func1<Identity, Observable<Identity>>() {
                                    @Override
                                    public Observable<Identity> call(final Identity identity) {
                                        if (TextUtils.isEmpty(mPurchaseGroupId)
                                                || Objects.equals(mPurchaseGroupId, identity.getGroup())) {
                                            return Observable.just(identity);
                                        }

                                        return mUserRepo.switchGroup(user, mPurchaseGroupId)
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
                        mMoneyFormatter = MoneyUtils.getMoneyFormatter(currency, true, true);
                    }
                })
                .flatMap(new Func1<Identity, Observable<Purchase>>() {
                    @Override
                    public Observable<Purchase> call(final Identity identity) {
                        return mPurchaseRepo.observePurchase(mPurchaseId, false)
                                .doOnNext(new Action1<Purchase>() {
                                    @Override
                                    public void call(Purchase purchase) {
                                        mForeignMoneyFormatter = MoneyUtils.getMoneyFormatter(purchase.getCurrency(), true, true);
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
                                        return mUserRepo.getIdentity(identityId).toObservable();
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
                        mIdentityItems.clear();
                        mIdentityItems.addAll(itemModels);
                        mIdentitiesListInteraction.notifyDataSetChanged();

                        checkIdentitiesActive(itemModels);
                    }
                })
                .subscribe(new IndefiniteSubscriber<List<PurchaseDetailsIdentityItemModel>>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        mView.showMessage(R.string.toast_error_purchase_details_load);
                    }

                    @Override
                    public void onNext(List<PurchaseDetailsIdentityItemModel> itemModels) {
                        setLoading(false);
                        mView.startEnterTransition();
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
        mExchangeRate = purchase.getExchangeRate();
        setMyShareForeign(myShare / mExchangeRate);
        setNote(purchase.getNote());
        setReceipt(purchase.getReceipt());
        setItems(purchase.getItems(), identityId);
    }

    private void setItems(@NonNull List<Item> items, @NonNull String identityId) {
        mItems.clear();
        for (Item item : items) {
            final PurchaseDetailsItemModel itemModel =
                    new PurchaseDetailsItemModel(item, identityId, mMoneyFormatter);
            mItems.add(itemModel);
        }
        mItemsListInteraction.notifyDataSetChanged();
    }

    private void checkIdentitiesActive(@NonNull List<PurchaseDetailsIdentityItemModel> itemModels) {
        for (PurchaseDetailsIdentityItemModel itemModel : itemModels) {
            if (!itemModel.isActive()) {
                mIdentitiesActive = false;
                break;
            }
        }
    }

    private void updateActionBarMenu(@NonNull Purchase purchase,
                                     @NonNull String groupCurrency,
                                     @NonNull String currentIdentity) {
        final boolean showEdit = Objects.equals(purchase.getBuyer(), currentIdentity);
        final boolean showExchangeRate = !Objects.equals(groupCurrency, purchase.getCurrency());
        mView.toggleMenuOptions(showEdit, showExchangeRate);
    }

    private void updateReadBy(@NonNull Purchase purchase, @NonNull String currentIdentity) {
        if (!purchase.isRead(currentIdentity)) {
            mPurchaseRepo.updateReadBy(mPurchaseId, currentIdentity);
        }
    }

    @Override
    public void onEditPurchaseClick() {
        if (mIdentitiesActive) {
            mNavigator.startPurchaseEdit(mPurchaseId, false);
        } else {
            mView.showMessage(R.string.toast_purchase_edit_identities_inactive);
        }
    }

    @Override
    public void onDeletePurchaseClick() {
        if (mIdentitiesActive) {
            mPurchaseRepo.deletePurchase(mPurchaseId, false);
            mNavigator.finish(PurchaseDetailsResult.PURCHASE_DELETED, mPurchaseId);
        } else {
            mView.showMessage(R.string.toast_purchase_edit_identities_inactive);
        }
    }

    @Override
    public void onShowExchangeRateClick() {
        final NumberFormat formatter = MoneyUtils.getExchangeRateFormatter();
        mView.showMessage(R.string.toast_exchange_rate_value, formatter.format(mExchangeRate));
    }

    @Override
    @Bindable
    public boolean isEmpty() {
        return mItems.isEmpty();
    }

    @Override
    public PurchaseDetailsItemModel getItemAtPosition(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        throw new RuntimeException("only one view type supported in this view!");
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public PurchaseDetailsIdentityItemModel getIdentityAtPosition(int position) {
        return mIdentityItems.get(position);
    }

    @Override
    public int getIdentitiesCount() {
        return mIdentityItems.size();
    }
}
