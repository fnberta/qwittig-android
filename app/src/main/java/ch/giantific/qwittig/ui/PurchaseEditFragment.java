package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.text.TextUtils;

import com.google.common.primitives.Booleans;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.data.models.ItemUsersChecked;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.rates.RestClient;
import ch.giantific.qwittig.data.rates.models.CurrencyRates;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class PurchaseEditFragment extends PurchaseBaseFragment implements
        LocalQuery.ObjectLocalFetchListener,
        CloudCode.CloudFunctionListener {

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
    ParseFile mReceiptFileNew;
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
        List<ParseObject> itemsNew = new ArrayList<>();
        int oldItemsSize = mOldItems.size();
        for (int i = 0; i < oldItemsSize; i++) {
            final Item itemOld = (Item) mOldItems.get(i);
            final Item itemNew = (Item) addNewItemRow(i + 1);
            itemNew.setEditTextName(itemOld.getName());
            String price = MoneyUtils.formatMoneyNoSymbol(itemOld.getPriceForeign(mOldExchangeRate),
                    mCurrencySelected);
            itemNew.setEditTextPrice(price);
            itemsNew.add(itemNew);

            // update ImeOptions
            setEditTextPriceImeOptions();

            // update usersInvolved for each item
            List<ParseUser> usersInvolved = itemOld.getUsersInvolved();
            ItemUsersChecked itemUsersChecked = mItemsUsersChecked.get(i);
            itemUsersChecked.setUsersChecked(ParseUserToBoolean(usersInvolved));
            if (buyerIsOnlyUserInvolved(usersInvolved)) {
                itemNew.setCheckBoxChecked(false);
            }
        }
        Item firstItem = (Item) itemsNew.get(0);
        if (firstItem != null) {
            firstItem.requestFocusForName();
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
        mReceiptFileNew = mListener.getReceiptParseFile();
        if (mReceiptFileNew != null) {
            mPurchase.setReceiptParseFile(mReceiptFileNew);
        }

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
            checkIfReceiptNull();
        } else {
            RestClient.getService().getRates(mCurrencySelected, new Callback<CurrencyRates>() {
                @Override
                public void success(CurrencyRates currencyRates, Response response) {
                    Map<String, Double> exchangeRates = currencyRates.getRates();
                    double exchangeRate = exchangeRates.get(mCurrentGroupCurrency);
                    mPurchase.setExchangeRate(exchangeRate);

                    checkIfReceiptNull();
                }

                @Override
                public void failure(RetrofitError error) {
                    onParseError(ParseUtils.getNoConnectionException(error.toString()));
                }
            });
        }
    }

    void checkIfReceiptNull() {
        if (mReceiptFileNew != null) {
            if (mReceiptFileOld != null) {
                deleteOldReceiptFile();
            } else {
                saveReceiptFile();
            }
        } else {
            if (mReceiptFileOld != null) {
                deleteOldReceiptFile();
                mPurchase.removeReceiptParseFile();
            } else {
                savePurchaseInParse();
            }
        }
    }

    private void deleteOldReceiptFile() {
        String fileName = mReceiptFileOld.getName();
        if (!TextUtils.isEmpty(fileName)) {
            CloudCode.deleteParseFile(getActivity(), fileName, this);
        }
    }

    @Override
    public void onCloudFunctionError(ParseException e) {
        onParseError(e);
    }

    @Override
    public void onCloudFunctionReturned(String cloudFunction, Object o) {
        switch (cloudFunction) {
            case CloudCode.DELETE_PARSE_FILE:
                if (mReceiptFileNew != null) {
                    saveReceiptFile();
                } else {
                    savePurchaseInParse();
                }
                break;
        }
    }

    void saveReceiptFile() {
        mReceiptFileNew.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    onParseError(e);
                    return;
                }

                onReceiptFileSaved();
            }
        });
    }

    void onReceiptFileSaved() {
        savePurchaseInParse();
    }

    @Override
    protected void onSaveSucceeded() {
        deleteOldItems();
        pinPurchase(false);
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
        if (oldItemsSize != mItems.size()) {
            return true;
        }

        setItemValues(true);
        for (int i = 0; i < oldItemsSize; i++) {
            Item itemOld = (Item) mOldItems.get(i);
            Item itemNew = (Item) mItems.get(i);
            if (!itemOld.getName().equals(itemNew.getName()) ||
                    itemOld.getPriceForeign(mOldExchangeRate) != itemNew.getPrice()) {
                return true;
            }

            List<String> usersInvolvedOld = itemOld.getUsersInvolvedIds();
            List<String> usersInvolvedNew = itemNew.getUsersInvolvedIds();
            if (usersInvolvedOld.size() != usersInvolvedNew.size() ||
                    !usersInvolvedOld.equals(usersInvolvedNew)) {
                return true;
            }
        }

        ParseFile receiptNew = mListener.getReceiptParseFile();
        if (receiptNew == null && mReceiptFileOld != null ||
                receiptNew != null && !receiptNew.equals(mReceiptFileOld)) {
            return true;
        }

        return false;
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
