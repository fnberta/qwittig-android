package ch.giantific.qwittig.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;

import com.google.common.primitives.Booleans;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.data.models.ItemRow;
import ch.giantific.qwittig.data.models.ItemUsersChecked;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.helper.PurchaseEditSaveHelper;
import ch.giantific.qwittig.helper.PurchaseSaveHelper;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class PurchaseEditFragment extends PurchaseBaseFragment implements
        LocalQuery.ObjectLocalFetchListener {

    static final String BUNDLE_EDIT_PURCHASE_ID = "edit_purchase_id";
    private static final String STATE_ITEMS_SET = "items_set";
    private static final String STATE_OLD_ITEMS = "old_items";
    private static final String STATE_OLD_STORE = "old_store";
    private static final String STATE_OLD_DATE = "old_date";
    private static final String STATE_OLD_CURRENCY = "old_currency";
    private static final String STATE_OLD_EXCHANGE_RATE = "old_exchange_rate";
    private static final String LOG_TAG = PurchaseEditFragment.class.getSimpleName();
    String mEditPurchaseId;
    ParseFile mReceiptFileOld;
    private boolean mOldValuesAreSet;
    private List<ParseObject> mOldItems;
    private ArrayList<String> mOldItemIds;
    private String mOldStore;
    private Date mOldDate;
    private String mOldCurrency;
    private double mOldExchangeRate;

    public PurchaseEditFragment() {
    }

    public static PurchaseEditFragment newInstance(String editPurchaseId) {
        PurchaseEditFragment fragment = new PurchaseEditFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_EDIT_PURCHASE_ID, editPurchaseId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mEditPurchaseId = getArguments().getString(BUNDLE_EDIT_PURCHASE_ID);

        if (savedInstanceState != null) {
            mOldValuesAreSet = savedInstanceState.getBoolean(STATE_ITEMS_SET);
            mOldItemIds = savedInstanceState.getStringArrayList(STATE_OLD_ITEMS);
            mOldStore = savedInstanceState.getString(STATE_OLD_STORE);
            mOldDate = DateUtils.parseLongToDate(savedInstanceState.getLong(STATE_OLD_DATE));
            mOldCurrency = savedInstanceState.getString(STATE_OLD_CURRENCY);
            mOldExchangeRate = savedInstanceState.getDouble(STATE_OLD_EXCHANGE_RATE);
        } else {
            mOldValuesAreSet = false;
            mOldItemIds = new ArrayList<>();
        }
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
        LocalQuery.fetchObjectFromId(this, Purchase.CLASS, mEditPurchaseId);
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        processOldPurchase(object);
    }

    void processOldPurchase(ParseObject parseObject) {
        mPurchase = (Purchase) parseObject;

        checkForReceiptFile();

        if (!mOldValuesAreSet) {
            // get old items and save in class wide list
            mOldItems = mPurchase.getItems();
            for (ParseObject itemOld : mOldItems) {
                mOldItemIds.add(itemOld.getObjectId());
            }

            // call here because we need mPurchase and mOldItems to be set
            fetchUsersAvailable();

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

    void checkForReceiptFile() {
        mReceiptFileOld = mPurchase.getReceiptParseFile();
        if (mReceiptFileOld != null && mListener.getReceiptParseFile() == null) {
            mListener.setReceiptParseFile(mReceiptFileOld);
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    protected void setupPurchaseUsersInvolved() {
        mPurchaseUsersInvolved.clear();
        List<ParseUser> usersInvolved = mPurchase.getUsersInvolved();
        for (ParseUser parseUser : mUsersAvailableParse) {
            if (usersInvolved.contains(parseUser)) {
                mPurchaseUsersInvolved.add(true);
            } else {
                mPurchaseUsersInvolved.add(false);
            }
        }
    }

    @Override
    void setupUserLists(List<ParseUser> users) {
        super.setupUserLists(users);

        if (!mOldValuesAreSet) {
            restoreOldItemValues();

            // old values are transferred, use normal procedure on recreation
            mOldValuesAreSet = true;
        }
        updateCheckBoxesColor();
    }

    private void restoreOldItemValues() {
        List<ItemRow> itemRowsNew = new ArrayList<>();
        int oldItemsSize = mOldItems.size();
        for (int i = 0; i < oldItemsSize; i++) {
            final Item itemOld = (Item) mOldItems.get(i);
            final ItemRow itemRowNew = addNewItemRow(i + 1);
            itemRowNew.setEditTextName(itemOld.getName());
            String price = MoneyUtils.formatMoneyNoSymbol(itemOld.getPriceForeign(mOldExchangeRate),
                    mCurrencySelected);
            itemRowNew.setEditTextPrice(price);
            itemRowsNew.add(itemRowNew);

            // update ImeOptions
            setEditTextPriceImeOptions();

            // update usersInvolved for each item
            List<ParseUser> usersInvolved = itemOld.getUsersInvolved();
            itemRowNew.setUsersChecked(ParseUserToBoolean(usersInvolved));
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

    private boolean buyerIsOnlyUserInvolved(List<ParseUser> usersInvolved) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        return usersInvolved.size() == 1 && usersInvolved.get(0).getObjectId()
                .equals(currentUser.getObjectId());
    }

    private boolean[] ParseUserToBoolean(List<ParseUser> usersInvolved) {
        List<Boolean> usersInvolvedBoolean = new ArrayList<>();

        for (ParseUser parseUser : mUsersAvailableParse) {
            if (usersInvolved.contains(parseUser)) {
                usersInvolvedBoolean.add(true);
            } else {
                usersInvolvedBoolean.add(false);
            }
        }

        return Booleans.toArray(usersInvolvedBoolean);
    }

    @Override
    protected void setPurchase() {
        replacePurchaseData();
        resetReadBy();
        updateExchangeRate();
    }

    final void replacePurchaseData() {
        final List<ParseUser> globalUsersInvolvedParse =
                getParseUsersInvolvedFromBoolean(mPurchaseUsersInvolved);

        // Replace the old values with new ones
        mPurchase.replaceItems(mItems);
        mPurchase.replaceUsersInvolved(globalUsersInvolvedParse);
        mPurchase.setDate(mDateSelected);
        mPurchase.setStore(mStoreSelected);
        mPurchase.setTotalPrice(mTotalPrice);
        mPurchase.setCurrency(mCurrencySelected);
    }

    private void resetReadBy() {
        mPurchase.resetReadBy();
    }

    private void updateExchangeRate() {
        if (mCurrencySelected.equals(mCurrentGroupCurrency)) {
            mPurchase.setExchangeRate(1);
            savePurchaseWithHelper();
        } else {
            getExchangeRateWithHelper();
        }
    }

    @Override
    public void onRatesFetchSuccessful(Map<String, Double> exchangeRates) {
        double exchangeRate = exchangeRates.get(mCurrentGroupCurrency);
        mPurchase.setExchangeRate(exchangeRate);

        savePurchaseWithHelper();
    }

    @Override
    public void onRatesFetchFailed(String errorMessage) {
        super.onRatesFetchFailed(errorMessage);

        onParseError(ParseUtils.getNoConnectionException(errorMessage));
    }

    private void savePurchaseWithHelper() {
        ParseFile receiptParseFileNew = mListener.getReceiptParseFile();

        FragmentManager fragmentManager = getFragmentManager();
        PurchaseEditSaveHelper purchaseEditSaveHelper = (PurchaseEditSaveHelper)
                fragmentManager.findFragmentByTag(PURCHASE_SAVE_HELPER);;

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (purchaseEditSaveHelper == null) {
            purchaseEditSaveHelper = new PurchaseEditSaveHelper(mReceiptFileOld, receiptParseFileNew, mPurchase, isDraft());

            fragmentManager.beginTransaction()
                    .add(purchaseEditSaveHelper, PURCHASE_SAVE_HELPER)
                    .commit();
        }
    }

    boolean isDraft() {
        return false;
    }

    @Override
    public void onPurchaseSaveAndPinSucceeded() {
        deleteOldItems();

        super.onPurchaseSaveAndPinSucceeded();
    }

    /**
     * Deletes old items in database only after new purchase has been successfully saved.
     */
    private void deleteOldItems() {
        for (String itemId : mOldItemIds) {
            ParseObject item = ParseObject.createWithoutData(Item.CLASS, itemId);
            item.deleteEventually();
        }
    }

    public boolean changesWereMade() {
        if (mOldDate.compareTo(mDateSelected) != 0 || !mOldStore.equals(mStoreSelected) ||
                !mOldCurrency.equals(mCurrencySelected)) {
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
            if (!itemOld.getName().equals(itemRowNew.getEditTextName()) ||
                    itemOld.getPriceForeign(mOldExchangeRate) !=
                            itemRowNew.getEditTextPrice(mCurrencySelected).doubleValue()) {
                Log.e(LOG_TAG, "price or name different");
                return true;
            }

            List<String> usersInvolvedOld = itemOld.getUsersInvolvedIds();
            List<String> usersInvolvedNew = getParseUsersInvolvedIdsFromItemRow(itemRowNew);
            if (usersInvolvedOld.size() != usersInvolvedNew.size() ||
                    !usersInvolvedOld.equals(usersInvolvedNew)) {
                Log.e(LOG_TAG, "usersInvolved different");
                return true;
            }
        }

        ParseFile receiptNew = mListener.getReceiptParseFile();
        if (receiptNew == null && mReceiptFileOld != null ||
                receiptNew != null && !receiptNew.equals(mReceiptFileOld)) {
            Log.e(LOG_TAG, "receipt file different");
            return true;
        }

        return false;
    }

    private List<String> getParseUsersInvolvedIdsFromItemRow(ItemRow itemRow) {
        final List<String> usersInvolved = new ArrayList<>();

        boolean[] usersChecked = itemRow.getUsersChecked();
        for (int i = 0, mUsersAvailableParseSize = mUsersAvailableParse.size();
             i < mUsersAvailableParseSize; i++) {
            ParseUser parseUser = mUsersAvailableParse.get(i);
            if (usersChecked[i]) {
                usersInvolved.add(parseUser.getObjectId());
            }
        }
        return usersInvolved;
    }

    @Override
    protected void savePurchaseAsDraft() {
        // is never called here, only in EditDraftFragment
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesAreSet);
        outState.putStringArrayList(STATE_OLD_ITEMS, mOldItemIds);
        outState.putString(STATE_OLD_STORE, mOldStore);
        outState.putLong(STATE_OLD_DATE, DateUtils.parseDateToLong(mOldDate));
        outState.putString(STATE_OLD_CURRENCY, mOldCurrency);
    }
}
