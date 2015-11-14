/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.data.helpers.save.PurchaseEditSaveHelper;
import ch.giantific.qwittig.data.helpers.save.PurchaseSaveHelper;
import ch.giantific.qwittig.data.repositories.ParsePurchaseRepository;
import ch.giantific.qwittig.domain.models.ItemRow;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.ui.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * Displays the interface where the user can edit an already existing {@link Purchase}.
 * <p/>
 * Subclass of {@link PurchaseBaseFragment}.
 */
public class PurchaseEditFragment extends PurchaseBaseFragment {

    static final String BUNDLE_EDIT_PURCHASE_ID = "BUNDLE_EDIT_PURCHASE_ID";
    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    private static final String STATE_OLD_ITEM_IDS = "STATE_OLD_ITEM_IDS";
    private static final String STATE_OLD_STORE = "STATE_OLD_STORE";
    private static final String STATE_OLD_DATE = "STATE_OLD_DATE";
    private static final String STATE_OLD_CURRENCY = "STATE_OLD_CURRENCY";
    private static final String STATE_OLD_EXCHANGE_RATE = "STATE_OLD_EXCHANGE_RATE";
    private static final String STATE_OLD_NOTE = "STATE_OLD_NOTE";
    private static final String LOG_TAG = PurchaseEditFragment.class.getSimpleName();
    private static final String DISCARD_CHANGES_DIALOG = "DISCARD_CHANGES_DIALOG";
    String mEditPurchaseId;
    PurchaseRepository mPurchaseRepo;
    private boolean mOldValuesAreSet;
    private List<ParseObject> mOldItems;
    private ArrayList<String> mOldItemIds;
    private String mOldStore;
    private Date mOldDate;
    private String mOldCurrency;
    private float mOldExchangeRate;
    private String mOldNote;
    private boolean mDeleteOldReceipt;

    public PurchaseEditFragment() {
    }

    /**
     * Returns a new instance of {@link PurchaseEditFragment}.
     *
     * @param editPurchaseId the object id of the purchase to edit
     * @return a new instance of {@link PurchaseEditFragment}
     */
    @NonNull
    public static PurchaseEditFragment newInstance(String editPurchaseId) {
        PurchaseEditFragment fragment = new PurchaseEditFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_EDIT_PURCHASE_ID, editPurchaseId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mPurchaseRepo = new ParsePurchaseRepository();
        mEditPurchaseId = getArguments().getString(BUNDLE_EDIT_PURCHASE_ID, "");

        if (savedInstanceState != null) {
            mOldValuesAreSet = savedInstanceState.getBoolean(STATE_ITEMS_SET);
            ArrayList<String> oldIds = savedInstanceState.getStringArrayList(STATE_OLD_ITEM_IDS);
            mOldItemIds = oldIds != null ? oldIds : new ArrayList<String>();
            mOldStore = savedInstanceState.getString(STATE_OLD_STORE, "");
            mOldDate = DateUtils.parseLongToDate(savedInstanceState.getLong(STATE_OLD_DATE));
            mOldCurrency = savedInstanceState.getString(STATE_OLD_CURRENCY, "");
            mOldExchangeRate = savedInstanceState.getFloat(STATE_OLD_EXCHANGE_RATE);
            mOldNote = savedInstanceState.getString(STATE_OLD_NOTE, "");
        } else {
            mOldValuesAreSet = false;
            mOldItemIds = new ArrayList<>();
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesAreSet);
        outState.putStringArrayList(STATE_OLD_ITEM_IDS, mOldItemIds);
        outState.putString(STATE_OLD_STORE, mOldStore);
        outState.putLong(STATE_OLD_DATE, DateUtils.parseDateToLong(mOldDate));
        outState.putString(STATE_OLD_CURRENCY, mOldCurrency);
        outState.putFloat(STATE_OLD_EXCHANGE_RATE, mOldExchangeRate);
        outState.putString(STATE_OLD_NOTE, mOldNote);
    }

    @Override
    public void setupRows() {
        if (mOldValuesAreSet) {
            // mPurchaseUsersInvolved will not be empty, so setupPurchaseUsersInvolved will not be
            // called and mPurchase does not need to exist
            fetchUsersAvailable();
            super.setupRows();
        }

        fetchPurchase();
    }

    void fetchPurchase() {
        mPurchaseRepo.fetchPurchaseDataLocalAsync(mEditPurchaseId, new PurchaseRepository.GetPurchaseLocalListener() {
            @Override
            public void onPurchaseLocalLoaded(@NonNull Purchase purchase) {
                processOldPurchase(purchase);
            }
        });
    }

    void processOldPurchase(Purchase purchase) {
        mPurchase = purchase;

        if (!mOldValuesAreSet) {
            // get old items and save in class wide list
            mOldItems = mPurchase.getItems();
            for (ParseObject itemOld : mOldItems) {
                mOldItemIds.add(itemOld.getObjectId());
            }

            // call here because we need mPurchase and mOldItems to be set
            fetchUsersAvailable();

            // check if there is a receipt image file and update action bar menu accordingly
            mListener.setHasReceiptFile(hasOldReceiptFile());

            // set note to value from original purchase
            String oldNote = mPurchase.getNote();
            mOldNote = oldNote != null ? oldNote : "";
            mNote = mOldNote;
            // check if purchase has a note and update action bar menu accordingly
            mListener.setHasNote(!TextUtils.isEmpty(mOldNote));

            // set store to value from original purchase
            mOldStore = mPurchase.getStore();
            setStore(mOldStore, false);

            // set date to value from original purchase
            mOldDate = mPurchase.getDate();
            setDate(mOldDate);

            // set currency from original purchase
            mOldCurrency = mPurchase.getCurrency();
            setCurrency(mOldCurrency);

            // get original exchangeRate to convert prices
            mOldExchangeRate = mPurchase.getExchangeRate();
        }
    }

    boolean hasOldReceiptFile() {
        return getOldReceiptFile() != null;
    }

    ParseFile getOldReceiptFile() {
        return mPurchase.getReceiptParseFile();
    }

    @Override
    void updateExchangeRate() {
        if (!mOldValuesAreSet && !mOldCurrency.equals(mCurrentGroupCurrency)) {
            mExchangeRate = mOldExchangeRate;
            setExchangeRate();
        } else {
            super.updateExchangeRate();
        }
    }

    @Override
    protected void setupPurchaseUsersInvolved() {
        List<ParseUser> usersInvolved = mPurchase.getUsersInvolved();

        int usersAvailableParseSize = mUsersAvailableParse.size();
        mPurchaseUsersInvolved = new boolean[usersAvailableParseSize];
        for (int i = 0; i < usersAvailableParseSize; i++) {
            ParseUser parseUser = mUsersAvailableParse.get(i);
            mPurchaseUsersInvolved[i] = usersInvolved.contains(parseUser);
        }
    }

    @Override
    void setupUserLists(@NonNull List<ParseUser> users) {
        super.setupUserLists(users);

        if (!mOldValuesAreSet) {
            restoreOldItemValues();

            // old values are transferred, use normal procedure on recreation
            mOldValuesAreSet = true;
        }
    }

    private void restoreOldItemValues() {
        int oldItemsSize = mOldItems.size();
        List<ItemRow> itemRowsNew = new ArrayList<>(oldItemsSize);
        for (int i = 0; i < oldItemsSize; i++) {
            final Item itemOld = (Item) mOldItems.get(i);
            final ItemRow itemRowNew = addNewItemRow(i + 1);
            String price = MoneyUtils.formatMoneyNoSymbol(itemOld.getPriceForeign(mOldExchangeRate),
                    mCurrencySelected);
            itemRowNew.fillValues(itemOld.getName(), price);
            itemRowsNew.add(itemRowNew);

            // update ImeOptions
            setEditTextPriceImeOptions();

            // update usersInvolved for each item
            List<ParseUser> usersInvolved = itemOld.getUsersInvolved();
            itemRowNew.setUsersChecked(getBooleansFromParseUsers(usersInvolved));
            itemRowNew.setCheckBoxColor(mPurchaseUsersInvolved);
            if (buyerIsOnlyUserInvolved(usersInvolved)) {
                itemRowNew.setCheckBoxChecked(false);
            }
        }
        ItemRow firstItemRow = itemRowsNew.get(0);
        if (firstItemRow != null) {
            firstItemRow.requestFocusForName();
        }
        mItemRowCount = oldItemsSize;
    }

    private boolean buyerIsOnlyUserInvolved(@NonNull List<ParseUser> usersInvolved) {
        return usersInvolved.size() == 1 && usersInvolved.get(0).getObjectId()
                .equals(mCurrentUser.getObjectId());
    }

    @NonNull
    private boolean[] getBooleansFromParseUsers(@NonNull List<ParseUser> usersInvolved) {
        int usersAvailableParseSize = mUsersAvailableParse.size();
        boolean[] usersInvolvedBoolean = new boolean[usersAvailableParseSize];

        for (int i = 0; i < usersAvailableParseSize; i++) {
            ParseUser parseUser = mUsersAvailableParse.get(i);
            usersInvolvedBoolean[i] = usersInvolved.contains(parseUser);
        }

        return usersInvolvedBoolean;
    }

    @Override
    public void deleteReceipt() {
        mDeleteOldReceipt = true;
    }

    @Override
    protected void setPurchase() {
        replacePurchaseData();
        resetReadBy();
    }

    final void replacePurchaseData() {
        final List<ParseUser> globalUsersInvolvedParse =
                getParseUsersInvolvedFromBoolean(mPurchaseUsersInvolved);

        // Replace the old values with new ones
        mPurchase.replaceItems(mItems);
        mPurchase.setUsersInvolved(globalUsersInvolvedParse);
        mPurchase.setDate(mDateSelected);
        mPurchase.setStore(mStoreSelected);
        mPurchase.setTotalPrice(mTotalPrice);
        mPurchase.setCurrency(mCurrencySelected);
        mPurchase.setExchangeRate(mExchangeRate);
        if (TextUtils.isEmpty(mNote)) {
            mPurchase.removeNote();
        } else {
            mPurchase.setNote(mNote);
        }
    }

    private void resetReadBy() {
        mPurchase.resetReadBy(mCurrentUser);
    }

    @Override
    protected PurchaseSaveHelper getSaveHelper() {
        return new PurchaseEditSaveHelper(mPurchase, isDraft(), getOldReceiptFile(),
                mDeleteOldReceipt, mReceiptImagePath);
    }

    boolean isDraft() {
        return false;
    }

    @Override
    public void onPurchaseSavedAndPinned() {
        deleteOldItems();

        super.onPurchaseSavedAndPinned();
    }

    /**
     * Deletes old items in the Parse.com online database.
     * <p/>
     * Should be called only after the purchase with the new items were successfully saved.
     */
    private void deleteOldItems() {
        for (String itemId : mOldItemIds) {
            ParseObject item = ParseObject.createWithoutData(Item.CLASS, itemId);
            item.deleteEventually();
        }
    }

    /**
     * Checks whether the user made any changes. If yes, displays a dialog asking him/her if he/she
     * wants to discard the changes. If no, sets no changes activity result and finishes.
     */
    public void checkForChangesAndExit() {
        if (changesWereMade()) {
            showDiscardChangesDialog();
        } else {
            setResultForSnackbar(Activity.RESULT_CANCELED);
            finishPurchase();
        }
    }

    private boolean changesWereMade() {
        if (mOldDate.compareTo(mDateSelected) != 0 || !mOldStore.equals(mStoreSelected) ||
                !mOldCurrency.equals(mCurrencySelected) || !mOldNote.equals(mNote)) {
            return true;
        }

        // after recreation mOldItems is null, hence check for it and if true fetch it again
        if (mOldItems == null) {
            mOldItems = mPurchase.getItems();
        }
        int oldItemsSize = mOldItems.size();
        if (oldItemsSize != mItemRows.size()) {
            return true;
        }

        for (int i = 0; i < oldItemsSize; i++) {
            Item itemOld = (Item) mOldItems.get(i);
            ItemRow itemRowNew = mItemRows.get(i);
            if (!itemOld.getName().equals(itemRowNew.getEditTextName())) {
                return true;
            }

            int maxFractionDigits = MoneyUtils.getMaximumFractionDigits(mCurrencySelected);
            BigDecimal oldPrice = new BigDecimal(itemOld.getPriceForeign(mOldExchangeRate))
                    .setScale(maxFractionDigits, BigDecimal.ROUND_HALF_UP);
            if (oldPrice.compareTo(itemRowNew.getEditTextPrice(mCurrencySelected)) > 0) {
                return true;
            }

            List<String> usersInvolvedOld = itemOld.getUsersInvolvedIds();
            List<String> usersInvolvedNew = itemRowNew.getParseUsersInvolvedIds(mUsersAvailableParse);
            if (!usersInvolvedNew.containsAll(usersInvolvedOld) ||
                    !usersInvolvedOld.containsAll(usersInvolvedNew)) {
                return true;
            }
        }

        // TODO: check if receipt images changed, difficult as new parse file gets created in save helper

        return false;
    }

    private void showDiscardChangesDialog() {
        DiscardChangesDialogFragment discardChangesDialogFragment =
                new DiscardChangesDialogFragment();
        discardChangesDialogFragment.show(getFragmentManager(), DISCARD_CHANGES_DIALOG);
    }

    @Override
    protected void savePurchaseAsDraft() {
        // is never called here, only in EditDraftFragment
    }

    @Override
    protected PurchaseReceiptBaseFragment getReceiptFragment() {
        final boolean isDraft = isDraft();
        final String objectId = isDraft ? mPurchase.getDraftId() : mPurchase.getObjectId();
        return TextUtils.isEmpty(mReceiptImagePath) ?
                PurchaseReceiptEditFragment.newInstance(objectId, isDraft) :
                PurchaseReceiptAddFragment.newInstance(mReceiptImagePath);
    }
}
