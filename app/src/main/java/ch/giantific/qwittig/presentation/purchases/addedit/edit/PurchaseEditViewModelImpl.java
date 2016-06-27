/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit.edit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.bus.RxBus;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.purchases.addedit.PurchaseAddEditViewModel;
import ch.giantific.qwittig.presentation.purchases.addedit.add.PurchaseAddViewModelImpl;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItem;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel;
import ch.giantific.qwittig.presentation.purchases.addedit.itemmodels.PurchaseAddEditItemModel.Type;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link PurchaseAddEditViewModel} for the edit purchase screen.
 * <p/>
 * Subclass of {@link PurchaseAddViewModelImpl}.
 */
public class PurchaseEditViewModelImpl extends PurchaseAddViewModelImpl {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    final String mEditPurchaseId;
    final Navigator mNavigator;
    Purchase mEditPurchase;
    boolean mDeleteOldReceipt;
    private boolean mOldValuesSet;

    public PurchaseEditViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull Navigator navigator,
                                     @NonNull RxBus<Object> eventBus,
                                     @NonNull UserRepository userRepository,
                                     @NonNull PurchaseRepository purchaseRepo,
                                     @NonNull String editPurchaseId) {
        super(savedState, navigator, eventBus, userRepository, purchaseRepo);

        mNavigator = navigator;
        mEditPurchaseId = editPurchaseId;

        if (savedState != null) {
            mOldValuesSet = savedState.getBoolean(STATE_ITEMS_SET, false);
        } else {
            mOldValuesSet = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesSet);
    }

    @Override
    public void loadData() {
        getSubscriptions().add(mUserRepo.getIdentities(mCurrentGroup, true)
                .toSortedList()
                .toSingle()
                .doOnSuccess(new Action1<List<Identity>>() {
                    @Override
                    public void call(List<Identity> identities) {
                        identities.remove(mCurrentIdentity);
                        identities.add(0, mCurrentIdentity);
                        mIdentities = identities;
                    }
                })
                .flatMap(new Func1<List<Identity>, Single<Purchase>>() {
                    @Override
                    public Single<Purchase> call(List<Identity> identities) {
                        return fetchEditPurchase();
                    }
                })
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        mEditPurchase = purchase;

                        if (mOldValuesSet) {
                            updateRows();
                        } else {
                            setOldPurchaseValues();
                            setOldItemValues();
                            mOldValuesSet = true;
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        mView.showMessage(R.string.toast_error_purchase_edit_load);
                    }
                })
        );
    }

    Single<Purchase> fetchEditPurchase() {
        return mPurchaseRepo.fetchPurchaseData(mEditPurchaseId);
    }

    private void setOldPurchaseValues() {
        mNote = mEditPurchase.getNoteOrEmpty();
        mView.reloadOptionsMenu();

        setStore(mEditPurchase.getStore());
        setDate(mEditPurchase.getDate());
        setCurrency(mEditPurchase.getCurrency());
        setExchangeRate(mEditPurchase.getExchangeRate());
        setReceiptImage(mEditPurchase.getReceiptUrl());
    }

    private void setOldItemValues() {
        final List<Item> oldItems = mEditPurchase.getItems();
        final Set<String> oldItemIds = new HashSet<>(oldItems.size());
        for (Item item : oldItems) {
            final List<Identity> identities = item.getIdentities();
            final String price = mMoneyFormatter.format(item.getPriceForeign(mExchangeRate));
            final PurchaseAddEditItem purchaseAddEditItem =
                    new PurchaseAddEditItem(item.getName(), price, getItemUsers(identities));
            purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
            purchaseAddEditItem.setPriceChangedListener(this);
            mItems.add(getLastPosition() - 1, purchaseAddEditItem);
            mListInteraction.notifyItemInserted(mItems.indexOf(purchaseAddEditItem));

            oldItemIds.add(item.getObjectId());
        }

        mPurchaseRepo.cacheOldEditItems(oldItemIds);
    }

    @NonNull
    @Override
    protected Purchase createPurchase(@NonNull List<Identity> purchaseIdentities,
                                      @NonNull List<Item> purchaseItems, int fractionDigits) {
        mEditPurchase.replaceItems(purchaseItems);
        mEditPurchase.setIdentities(purchaseIdentities);
        mEditPurchase.setDate(mDate);
        mEditPurchase.setStore(mStore);
        final BigDecimal totalPriceRounded =
                new BigDecimal(mTotalPrice).setScale(fractionDigits, BigDecimal.ROUND_HALF_UP);
        mEditPurchase.setTotalPrice(totalPriceRounded);
        mEditPurchase.setCurrency(mCurrency);
        mEditPurchase.setExchangeRate(mExchangeRate);
        if (TextUtils.isEmpty(mNote)) {
            mEditPurchase.removeNote();
        } else {
            mEditPurchase.setNote(mNote);
        }
        if (TextUtils.isEmpty(mReceiptImagePath)) {
            mEditPurchase.removeReceipt();
        } else if (!Objects.equals(mReceiptImagePath, mEditPurchase.getReceiptUrl())) {
            mEditPurchase.setReceiptLocal(mReceiptImagePath);
        }
        mEditPurchase.resetReadBy(mCurrentIdentity);

        return mEditPurchase;
    }

    @Override
    public void onDeleteReceiptMenuClick() {
        super.onDeleteReceiptMenuClick();

        mDeleteOldReceipt = true;
    }

    @Override
    protected Single<Purchase> getSavePurchaseAction(@NonNull Purchase purchase) {
        return mPurchaseRepo.savePurchaseEdit(purchase, mDeleteOldReceipt);
    }

    @Override
    public void onExitClick() {
        if (changesWereMade()) {
            mView.showDiscardEditChangesDialog();
        } else {
            mNavigator.finish(Activity.RESULT_CANCELED);
        }
    }

    private boolean changesWereMade() {
        if (mEditPurchase.getDate().compareTo(mDate) != 0
                || !Objects.equals(mEditPurchase.getStore(), mStore)
                || !Objects.equals(mEditPurchase.getCurrency(), mCurrency)
                || !Objects.equals(mEditPurchase.getNoteOrEmpty(), mNote)) {
            return true;
        }

        final List<Item> oldItems = mEditPurchase.getItems();
        for (int i = 0, size = mItems.size(), skipCount = 0; i < size; i++) {
            final PurchaseAddEditItemModel addEditItem = mItems.get(i);
            if (addEditItem.getType() != Type.ITEM) {
                skipCount++;
                continue;
            }

            final Item itemOld;
            try {
                itemOld = oldItems.get(i - skipCount);
            } catch (IndexOutOfBoundsException e) {
                return true;
            }
            final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
            if (!Objects.equals(itemOld.getName(), purchaseAddEditItem.getName())) {
                return true;
            }

            final double oldPrice = itemOld.getPriceForeign(mEditPurchase.getExchangeRate());
            final double newPrice = purchaseAddEditItem.parsePrice();
            if (Math.abs(oldPrice - newPrice) >= MoneyUtils.MIN_DIFF) {
                return true;
            }

            final List<String> identitiesOld = itemOld.getIdentitiesIds();
            final List<String> identitiesNew = purchaseAddEditItem.getSelectedIdentitiesIds();
            if (!identitiesNew.containsAll(identitiesOld) ||
                    !identitiesOld.containsAll(identitiesNew)) {
                return true;
            }
        }

        if (mDeleteOldReceipt || !Objects.equals(mReceiptImagePath, mEditPurchase.getReceiptUrl())) {
            return true;
        }

        return false;
    }
}
