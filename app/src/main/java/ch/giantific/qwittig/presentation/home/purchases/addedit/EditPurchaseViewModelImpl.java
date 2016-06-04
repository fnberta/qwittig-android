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
import java.util.Date;
import java.util.List;

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
    private static final String STATE_OLD_STORE = "STATE_OLD_STORE";
    private static final String STATE_OLD_DATE = "STATE_OLD_DATE";
    private static final String STATE_OLD_CURRENCY = "STATE_OLD_CURRENCY";
    private static final String STATE_OLD_EXCHANGE_RATE = "STATE_OLD_EXCHANGE_RATE";
    private static final String STATE_OLD_NOTE = "STATE_OLD_NOTE";
    final String mEditPurchaseId;
    Purchase mEditPurchase;
    boolean mDeleteOldReceipt;
    private boolean mOldValuesSet;
    private String mOldStore;
    private Date mOldDate;
    private String mOldCurrency;
    private double mOldExchangeRate;
    private String mOldNote;

    public EditPurchaseViewModelImpl(@Nullable Bundle savedState,
                                     @NonNull AddEditPurchaseViewModel.ViewListener view,
                                     @NonNull UserRepository userRepository,
                                     @NonNull PurchaseRepository purchaseRepo,
                                     @NonNull String editPurchaseId) {
        super(savedState, view, userRepository, purchaseRepo);

        mEditPurchaseId = editPurchaseId;

        if (savedState != null) {
            mOldValuesSet = savedState.getBoolean(STATE_ITEMS_SET, false);
            mOldStore = savedState.getString(STATE_OLD_STORE, "");
            mOldDate = new Date(savedState.getLong(STATE_OLD_DATE));
            mOldCurrency = savedState.getString(STATE_OLD_CURRENCY, "");
            mOldExchangeRate = savedState.getDouble(STATE_OLD_EXCHANGE_RATE);
            mOldNote = savedState.getString(STATE_OLD_NOTE, "");
        } else {
            mOldValuesSet = false;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesSet);
        outState.putString(STATE_OLD_STORE, mOldStore);
        outState.putLong(STATE_OLD_DATE, mOldDate.getTime());
        outState.putString(STATE_OLD_CURRENCY, mOldCurrency);
        outState.putDouble(STATE_OLD_EXCHANGE_RATE, mOldExchangeRate);
        outState.putString(STATE_OLD_NOTE, mOldNote);
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
        // check if there is a receipt image file and update action bar menu accordingly
        mView.toggleReceiptMenuOption(hasOldReceiptFile());

        // set note to value from original purchase
        final String oldNote = mEditPurchase.getNote();
        mOldNote = oldNote != null ? oldNote : "";
        mNote = mOldNote;
        // check if purchase has a note and update action bar menu accordingly
        mView.toggleNoteMenuOption(!TextUtils.isEmpty(mNote));

        // set store to value from original purchase
        mOldStore = mEditPurchase.getStore();
        setStore(mOldStore);

        // set date to value from original purchase
        mOldDate = mEditPurchase.getDate();
        setDate(mOldDate);

        // set currency from original purchase
        mOldCurrency = mEditPurchase.getCurrency();
        setCurrency(mOldCurrency);

        // get original exchangeRate to convert prices
        mOldExchangeRate = mEditPurchase.getExchangeRate();
        setExchangeRate(mOldExchangeRate);
    }

    boolean hasOldReceiptFile() {
        return mEditPurchase.getReceipt() != null;
    }

    private void setOldItemValues() {
        final List<Item> oldItems = mEditPurchase.getItems();
        for (Item item : oldItems) {
            final List<Identity> identities = item.getIdentities();
            final String price = mMoneyFormatter.format(item.getPriceForeign(mOldExchangeRate));
            final AddEditPurchaseItem purchaseAddEditItem =
                    new AddEditPurchaseItem(item.getName(), price, getItemUsers(identities));
            purchaseAddEditItem.setMoneyFormatter(mMoneyFormatter);
            purchaseAddEditItem.setPriceChangedListener(this);
            mItems.add(getLastPosition() - 1, purchaseAddEditItem);
            mView.notifyItemInserted(mItems.indexOf(purchaseAddEditItem));
        }
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
        mView.showReceiptImage(mEditPurchaseId, mReceiptImagePath);
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
    void askToDiscard() {
        if (changesWereMade()) {
            mView.showDiscardEditChangesDialog();
        } else {
            mView.finishScreen(Activity.RESULT_CANCELED);
        }
    }

    private boolean changesWereMade() {
        if (mOldDate.compareTo(mDate) != 0 || !mOldStore.equals(mStore) ||
                !mOldCurrency.equals(mCurrency) || !mOldNote.equals(mNote)) {
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

            final double oldPrice = itemOld.getPriceForeign(mOldExchangeRate);
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
