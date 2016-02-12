/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.ParseObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Item;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditItem;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.AddEditItem.Type;
import ch.giantific.qwittig.presentation.home.purchases.addedit.items.ItemItem;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by fabio on 28.01.16.
 */
public class PurchaseAddEditViewModelEditImpl extends PurchaseAddEditViewModelAddImpl {

    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    private static final String STATE_OLD_ITEM_IDS = "STATE_OLD_ITEM_IDS";
    private static final String STATE_OLD_STORE = "STATE_OLD_STORE";
    private static final String STATE_OLD_DATE = "STATE_OLD_DATE";
    private static final String STATE_OLD_CURRENCY = "STATE_OLD_CURRENCY";
    private static final String STATE_OLD_EXCHANGE_RATE = "STATE_OLD_EXCHANGE_RATE";
    private static final String STATE_OLD_NOTE = "STATE_OLD_NOTE";
    String mEditPurchaseId;
    Purchase mEditPurchase;
    boolean mDeleteOldReceipt;
    private boolean mOldValuesSet;
    private List<Item> mOldItems;
    private ArrayList<String> mOldItemIds;
    private String mOldStore;
    private Date mOldDate;
    private String mOldCurrency;
    private float mOldExchangeRate;
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
            mOldDate = DateUtils.parseLongToDate(savedState.getLong(STATE_OLD_DATE));
            mOldCurrency = savedState.getString(STATE_OLD_CURRENCY, "");
            mOldExchangeRate = savedState.getFloat(STATE_OLD_EXCHANGE_RATE);
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
        outState.putLong(STATE_OLD_DATE, DateUtils.parseDateToLong(mOldDate));
        outState.putString(STATE_OLD_CURRENCY, mOldCurrency);
        outState.putFloat(STATE_OLD_EXCHANGE_RATE, mOldExchangeRate);
        outState.putString(STATE_OLD_NOTE, mOldNote);
    }

    @Override
    void onIdentitiesReady() {
        mSubscriptions.add(fetchEditPurchase()
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
                        // TODO: handle error
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
            final String price = MoneyUtils.formatMoneyNoSymbol(
                    item.getPriceForeign(mOldExchangeRate), mCurrency);
            final List<Identity> identities = item.getIdentities();
            final ItemItem itemItem = new ItemItem(item.getName(), price,
                    getItemUsersItemUsers(identities), mCurrency);
            // TODO: don't hardcode add row position
            mItems.add(getLastPosition() - 1, itemItem);
            mView.notifyItemInserted(mItems.indexOf(itemItem));
        }
    }

    @NonNull
    @Override
    Purchase createPurchase(@NonNull List<Identity> purchaseIdentities,
                            @NonNull List<Item> purchaseItems) {
        mEditPurchase.replaceItems(purchaseItems);
        mEditPurchase.setIdentities(purchaseIdentities);
        mEditPurchase.setDate(mDate);
        mEditPurchase.setStore(mStore);
        mEditPurchase.setTotalPrice(mTotalPrice);
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

        // TODO: maybe drop size comparison, requires loop anyway
        final int oldItemsSize = mOldItems.size();
        int newItemsSize = 0;
        for (AddEditItem addEditItem : mItems) {
            if (addEditItem.getType() == Type.ITEM) {
                newItemsSize++;
            }
        }
        if (oldItemsSize != newItemsSize) {
            return true;
        }

        final int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(mCurrency);
        for (int i = 0, size = mItems.size(), skipCount = 0; i < size; i++) {
            final AddEditItem addEditItem = mItems.get(i);
            if (addEditItem.getType() != Type.ITEM) {
                skipCount++;
                continue;
            }

            final Item itemOld = mOldItems.get(i - skipCount);
            final ItemItem itemItem = (ItemItem) addEditItem;
            if (!itemOld.getName().equals(itemItem.getName())) {
                return true;
            }

            final BigDecimal oldPrice = new BigDecimal(itemOld.getPriceForeign(mOldExchangeRate))
                    .setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP);
            if (oldPrice.compareTo(itemItem.parsePrice(mCurrency)) > 0) {
                return true;
            }

            final List<String> usersInvolvedOld = itemOld.getIdentitiesIds();
            final List<String> usersInvolvedNew = itemItem.getSelectedUserIds();
            if (!usersInvolvedNew.containsAll(usersInvolvedOld) ||
                    !usersInvolvedOld.containsAll(usersInvolvedNew)) {
                return true;
            }
        }

        if (mDeleteOldReceipt || !TextUtils.isEmpty(mReceiptImagePath)) {
            return true;
        }

        return false;
    }
}
