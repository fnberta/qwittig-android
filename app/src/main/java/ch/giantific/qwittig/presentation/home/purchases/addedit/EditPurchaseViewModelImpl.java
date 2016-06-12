/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseBaseItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseBaseItem.Type;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditPurchaseItem;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Provides an implementation of the {@link AddEditPurchaseViewModel} for the edit purchase screen.
 * <p/>
 * Subclass of {@link AddPurchaseViewModelImpl}.
 */
public class EditPurchaseViewModelImpl extends AddPurchaseViewModelImpl {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    final String mEditPurchaseId;
    Purchase mEditPurchase;
    boolean mDeleteOldReceipt;
    private boolean mOldValuesSet;

    public EditPurchaseViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull AddEditPurchaseViewModel.ViewListener view,
                                     @NonNull UserRepository userRepository,
                                     @NonNull PurchaseRepository purchaseRepo,
                                     @NonNull String editPurchaseId) {
        super(savedState, view, userRepository, purchaseRepo);

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
        mView.toggleReceiptMenuOption(mEditPurchase.hasReceipt());

        final String oldNote = mEditPurchase.getNote();
        mNote = oldNote != null ? oldNote : "";
        mView.toggleNoteMenuOption(!TextUtils.isEmpty(mNote));

        setStore(mEditPurchase.getStore());
        setDate(mEditPurchase.getDate());
        setCurrency(mEditPurchase.getCurrency());
        setExchangeRate(mEditPurchase.getExchangeRate());
    }

    private void setOldItemValues() {
        final List<Item> oldItems = mEditPurchase.getItems();
        final Set<String> oldItemIds = new HashSet<>(oldItems.size());
        for (Item item : oldItems) {
            final List<Identity> identities = item.getIdentities();
            final String price = mMoneyFormatter.format(item.getPriceForeign(mExchangeRate));
            final AddEditPurchaseItem purchaseAddEditItem =
                    new AddEditPurchaseItem(item.getName(), price, getItemUsers(identities));
            purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
            purchaseAddEditItem.setPriceChangedListener(this);
            mItems.add(getLastPosition() - 1, purchaseAddEditItem);
            mView.notifyItemInserted(mItems.indexOf(purchaseAddEditItem));

            oldItemIds.add(item.getObjectId());
        }

        mPurchaseRepo.cacheOldEditItems(oldItemIds);
    }

    @NonNull
    @Override
    Purchase createPurchase(@NonNull List<Identity> purchaseIdentities,
                            @NonNull List<Item> purchaseItems, int fractionDigits) {
        mEditPurchase.replaceItems(purchaseItems);
        mEditPurchase.setIdentities(purchaseIdentities);
        mEditPurchase.setDate(mDate);
        mEditPurchase.setStore(mStore);
        final BigDecimal totalPriceRounded =
                new BigDecimal(mTotalPrice).setScale(fractionDigits, BigDecimal.ROUND_UP);
        mEditPurchase.setTotalPrice(totalPriceRounded);
        mEditPurchase.setCurrency(mCurrency);
        mEditPurchase.setExchangeRate(mExchangeRate);
        if (TextUtils.isEmpty(mNote)) {
            mEditPurchase.removeNote();
        } else {
            mEditPurchase.setNote(mNote);
        }
        mEditPurchase.resetReadBy(mCurrentIdentity);

        return mEditPurchase;
    }

    @Override
    public void onShowReceiptImageMenuClick() {
        mView.showReceiptImage(!TextUtils.isEmpty(mReceiptImagePath) ? mReceiptImagePath : mEditPurchase.getReceiptUrl());
    }

    @Override
    public void onDeleteReceiptImageMenuClick() {
        super.onDeleteReceiptImageMenuClick();

        mDeleteOldReceipt = true;
    }

    @Override
    Single<Purchase> getSavePurchaseAction(@NonNull Purchase purchase) {
        return mPurchaseRepo.savePurchaseEdit(purchase, mDeleteOldReceipt);
    }

    @Override
    public void onExitClick() {
        if (changesWereMade()) {
            mView.showDiscardEditChangesDialog();
        } else {
            mView.finishScreen(Activity.RESULT_CANCELED);
        }
    }

    private boolean changesWereMade() {
        if (mEditPurchase.getDate().compareTo(mDate) != 0 || !mEditPurchase.getStore().equals(mStore) ||
                !mEditPurchase.getCurrency().equals(mCurrency) || !mEditPurchase.getNote().equals(mNote)) {
            return true;
        }

        final List<Item> oldItems = mEditPurchase.getItems();
        for (int i = 0, size = mItems.size(), skipCount = 0; i < size; i++) {
            final AddEditPurchaseBaseItem addEditItem = mItems.get(i);
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
            final AddEditPurchaseItem purchaseAddEditItem = (AddEditPurchaseItem) addEditItem;
            if (!itemOld.getName().equals(purchaseAddEditItem.getName())) {
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

        if (mDeleteOldReceipt || !TextUtils.isEmpty(mReceiptImagePath)) {
            return true;
        }

        return false;
    }
}
