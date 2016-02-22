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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditBaseItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditBaseItem.Type;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.PurchaseAddEditItem;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Provides an implementation of the {@link PurchaseAddEditViewModel} for the edit purchase screen.
 * <p/>
 * Subclass of {@link PurchaseAddEditViewModelAddImpl}.
 */
public class PurchaseAddEditViewModelEditImpl extends PurchaseAddEditViewModelAddImpl {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    private static final String STATE_OLD_ITEM_IDS = "STATE_OLD_ITEM_IDS";
    private static final String STATE_OLD_STORE = "STATE_OLD_STORE";
    private static final String STATE_OLD_DATE = "STATE_OLD_DATE";
    private static final String STATE_OLD_CURRENCY = "STATE_OLD_CURRENCY";
    private static final String STATE_OLD_EXCHANGE_RATE = "STATE_OLD_EXCHANGE_RATE";
    private static final String STATE_OLD_NOTE = "STATE_OLD_NOTE";
    final String mEditPurchaseId;
    private final ArrayList<String> mOldItemIds;
    Purchase mEditPurchase;
    boolean mDeleteOldReceipt;
    private boolean mOldValuesSet;
    private List<Item> mOldItems;
    private String mOldStore;
    private Date mOldDate;
    private String mOldCurrency;
    private double mOldExchangeRate;
    private String mOldNote;

    public PurchaseAddEditViewModelEditImpl(@Nullable Bundle savedState,
                                            @NonNull PurchaseAddEditViewModel.ViewListener view,
                                            @NonNull IdentityRepository identityRepository,
                                            @NonNull UserRepository userRepository,
                                            @NonNull PurchaseRepository purchaseRepo,
                                            @NonNull String editPurchaseId) {
        super(savedState, view, identityRepository, userRepository, purchaseRepo);

        mEditPurchaseId = editPurchaseId;

        if (savedState != null) {
            mOldValuesSet = savedState.getBoolean(STATE_ITEMS_SET);
            mOldItemIds = savedState.getStringArrayList(STATE_OLD_ITEM_IDS);
            mOldStore = savedState.getString(STATE_OLD_STORE, "");
            mOldDate = new Date(savedState.getLong(STATE_OLD_DATE));
            mOldCurrency = savedState.getString(STATE_OLD_CURRENCY, "");
            mOldExchangeRate = savedState.getDouble(STATE_OLD_EXCHANGE_RATE);
            mOldNote = savedState.getString(STATE_OLD_NOTE, "");
        } else {
            mOldValuesSet = false;
            mOldItemIds = new ArrayList<>();
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesSet);
        outState.putStringArrayList(STATE_OLD_ITEM_IDS, mOldItemIds);
        outState.putString(STATE_OLD_STORE, mOldStore);
        outState.putLong(STATE_OLD_DATE, mOldDate.getTime());
        outState.putString(STATE_OLD_CURRENCY, mOldCurrency);
        outState.putDouble(STATE_OLD_EXCHANGE_RATE, mOldExchangeRate);
        outState.putString(STATE_OLD_NOTE, mOldNote);
    }

    @Override
    void onIdentitiesReady() {
        if (mOldValuesSet) {
            super.onIdentitiesReady();
        }

        getSubscriptions().add(fetchEditPurchase()
                .subscribe(new SingleSubscriber<Purchase>() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        mEditPurchase = purchase;

                        if (!mOldValuesSet) {
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
        return mPurchaseRepo.fetchPurchaseDataLocalAsync(mEditPurchaseId);
    }

    private void setOldPurchaseValues() {
        // get old items and save in class wide list
        mOldItems = mEditPurchase.getItems();
        for (Item itemOld : mOldItems) {
            mOldItemIds.add(itemOld.getObjectId());
        }

        // check if there is a receipt image file and update action bar menu accordingly
        mView.toggleReceiptMenuOption(hasOldReceiptFile());

        // set note to value from original purchase
        String oldNote = mEditPurchase.getNote();
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
        for (Item item : mOldItems) {
            final List<Identity> identities = item.getIdentities();
            final String price = mMoneyFormatter.format(item.getPriceForeign(mOldExchangeRate));
            final PurchaseAddEditItem purchaseAddEditItem = new PurchaseAddEditItem(item.getName(), price, getItemUsersItemUsers(identities));
            mItems.add(getLastPosition() - 1, purchaseAddEditItem);
            mView.notifyItemInserted(mItems.indexOf(purchaseAddEditItem));
        }

        super.onIdentitiesReady();
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
    public void onShowReceiptImageClick() {
        mView.showReceiptImage(mEditPurchaseId, mReceiptImagePath, false);
    }

    @Override
    public void onDeleteReceiptImageClick() {
        super.onDeleteReceiptImageClick();

        mDeleteOldReceipt = true;
    }

    @Override
    void loadSavePurchaseWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage) {
        mView.loadSavePurchaseWorker(purchase, receiptImage, purchase.getReceipt(),
                mDeleteOldReceipt, false);
    }

    @Override
    void onPurchaseSaved() {
        deleteOldItems();
        super.onPurchaseSaved();
    }

    /**
     * Deletes old items in the online database.
     * <p/>
     * Should be called only after the purchase with the new items were successfully saved.
     */
    private void deleteOldItems() {
        mPurchaseRepo.deleteItemsByIds(mOldItemIds);
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

        // after recreation mOldItems is null, hence check for it and if true fetch it again
        if (mOldItems == null) {
            mOldItems = mEditPurchase.getItems();
        }

        for (int i = 0, size = mItems.size(), skipCount = 0; i < size; i++) {
            final PurchaseAddEditBaseItem addEditItem = mItems.get(i);
            if (addEditItem.getType() != Type.ITEM) {
                skipCount++;
                continue;
            }

            final Item itemOld;
            try {
                itemOld = mOldItems.get(i - skipCount);
            } catch (IndexOutOfBoundsException e) {
                return true;
            }
            final PurchaseAddEditItem purchaseAddEditItem = (PurchaseAddEditItem) addEditItem;
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
