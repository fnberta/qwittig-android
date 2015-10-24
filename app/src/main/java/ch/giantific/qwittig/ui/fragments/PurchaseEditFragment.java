package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ItemRow;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.helpers.PurchaseEditSaveHelper;
import ch.giantific.qwittig.ui.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.DateUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.MoneyUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class PurchaseEditFragment extends PurchaseBaseFragment implements
        LocalQuery.ObjectLocalFetchListener {

    static final String BUNDLE_EDIT_PURCHASE_ID = "BUNDLE_EDIT_PURCHASE_ID";
    private static final String STATE_ITEMS_SET = "STATE_ITEMS_SET";
    private static final String STATE_OLD_ITEM_IDS = "STATE_OLD_ITEM_IDS";
    private static final String STATE_OLD_STORE = "STATE_OLD_STORE";
    private static final String STATE_OLD_DATE = "STATE_OLD_DATE";
    private static final String STATE_OLD_CURRENCY = "STATE_OLD_CURRENCY";
    private static final String STATE_OLD_EXCHANGE_RATE = "STATE_OLD_EXCHANGE_RATE";
    private static final String LOG_TAG = PurchaseEditFragment.class.getSimpleName();
    String mEditPurchaseId;
    private boolean mOldValuesAreSet;
    private List<ParseObject> mOldItems;
    private ArrayList<String> mOldItemIds;
    private String mOldStore;
    private Date mOldDate;
    private String mOldCurrency;
    private float mOldExchangeRate;
    private boolean mDeleteOldReceipt;

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
            mOldItemIds = savedInstanceState.getStringArrayList(STATE_OLD_ITEM_IDS);
            mOldStore = savedInstanceState.getString(STATE_OLD_STORE);
            mOldDate = DateUtils.parseLongToDate(savedInstanceState.getLong(STATE_OLD_DATE));
            mOldCurrency = savedInstanceState.getString(STATE_OLD_CURRENCY);
            mOldExchangeRate = savedInstanceState.getFloat(STATE_OLD_EXCHANGE_RATE);
        } else {
            mOldValuesAreSet = false;
            mOldItemIds = new ArrayList<>();
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_ITEMS_SET, mOldValuesAreSet);
        outState.putStringArrayList(STATE_OLD_ITEM_IDS, mOldItemIds);
        outState.putString(STATE_OLD_STORE, mOldStore);
        outState.putLong(STATE_OLD_DATE, DateUtils.parseDateToLong(mOldDate));
        outState.putString(STATE_OLD_CURRENCY, mOldCurrency);
        outState.putFloat(STATE_OLD_EXCHANGE_RATE, mOldExchangeRate);
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
        LocalQuery.fetchObjectFromId(Purchase.CLASS, mEditPurchaseId, this);
    }

    @Override
    public void onObjectFetched(ParseObject object) {
        processOldPurchase(object);
    }

    void processOldPurchase(ParseObject parseObject) {
        mPurchase = (Purchase) parseObject;

        if (!mOldValuesAreSet) {
            // get old items and save in class wide list
            mOldItems = mPurchase.getItems();
            for (ParseObject itemOld : mOldItems) {
                mOldItemIds.add(itemOld.getObjectId());
            }

            // call here because we need mPurchase and mOldItems to be set
            fetchUsersAvailable();

            // check if there is a receipt image file and update action bar menu accordingly
            mListener.updateActionBarMenu(getOldReceiptFile() != null);

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

    @Override
    void updateExchangeRate() {
        if (!mOldValuesAreSet && !mOldCurrency.equals(mCurrentGroupCurrency)) {
            mExchangeRate = mOldExchangeRate;
            setExchangeRate();
        } else {
            super.updateExchangeRate();
        }
    }

    ParseFile getOldReceiptFile() {
        return mPurchase.getReceiptParseFile();
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
        int oldItemsSize = mOldItems.size();
        List<ItemRow> itemRowsNew = new ArrayList<>(oldItemsSize);
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
        int usersAvailableParseSize = mUsersAvailableParse.size();
        boolean[] usersInvolvedBoolean = new boolean[usersAvailableParseSize];

        for (int i = 0; i < usersAvailableParseSize; i++) {
            ParseUser parseUser = mUsersAvailableParse.get(i);
            usersInvolvedBoolean[i] = usersInvolved.contains(parseUser);
        }

        return usersInvolvedBoolean;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                String message;
                if (getOldReceiptFile() != null) {
                    message = getString(R.string.toast_receipt_changed);
                } else {
                    message = getString(R.string.toast_receipt_added);
                }
                MessageUtils.showBasicSnackbar(mButtonAddRow, message);
            }
        }
    }

    @Override
    public void deleteReceipt() {
        mDeleteOldReceipt = true;
    }

    @Override
    protected void setPurchase() {
        replacePurchaseData();
        resetReadBy();
        savePurchaseWithHelper();
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
    }

    private void resetReadBy() {
        mPurchase.resetReadBy();
    }

    private void savePurchaseWithHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        PurchaseEditSaveHelper purchaseEditSaveHelper = (PurchaseEditSaveHelper)
                fragmentManager.findFragmentByTag(PURCHASE_SAVE_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (purchaseEditSaveHelper == null) {
            if (mDeleteOldReceipt) {
                purchaseEditSaveHelper = new PurchaseEditSaveHelper(mPurchase, isDraft(), getOldReceiptFile());
            } else {
                purchaseEditSaveHelper = new PurchaseEditSaveHelper(mPurchase, isDraft(),
                        getOldReceiptFile(), mReceiptImagePath);
            }

            fragmentManager.beginTransaction()
                    .add(purchaseEditSaveHelper, PURCHASE_SAVE_HELPER)
                    .commit();
        }
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
     * Deletes old items in database only after new purchase has been successfully saved.
     */
    private void deleteOldItems() {
        for (String itemId : mOldItemIds) {
            ParseObject item = ParseObject.createWithoutData(Item.CLASS, itemId);
            item.deleteEventually();
        }
    }

    public void checkForChangesAndExit() {
        if (changesWereMade()) {
            showDiscardChangesDialog();
        } else {
            setResultForSnackbar(PURCHASE_NO_CHANGES);
            finishPurchase();
        }
    }

    private void showDiscardChangesDialog() {
        DiscardChangesDialogFragment discardChangesDialogFragment =
                new DiscardChangesDialogFragment();
        discardChangesDialogFragment.show(getFragmentManager(), "discard_changes");
    }

    private boolean changesWereMade() {
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
            List<String> usersInvolvedNew = getParseUsersInvolvedIdsFromItemRow(itemRowNew);
            if (usersInvolvedOld.size() != usersInvolvedNew.size() ||
                    !usersInvolvedOld.equals(usersInvolvedNew)) {
                return true;
            }
        }

        // TODO: fix
//        if (receiptNew == null && mReceiptFileOld != null ||
//                receiptNew != null && !receiptNew.equals(mReceiptFileOld)) {
//            return true;
//        }

        return false;
    }

    private List<String> getParseUsersInvolvedIdsFromItemRow(ItemRow itemRow) {
        int usersAvailableParseSize = mUsersAvailableParse.size();
        final List<String> usersInvolved = new ArrayList<>(usersAvailableParseSize);

        boolean[] usersChecked = itemRow.getUsersChecked();
        for (int i = 0; i < usersAvailableParseSize; i++) {
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
    protected PurchaseReceiptBaseFragment getReceiptFragment() {
        return PurchaseReceiptEditFragment.newInstance(mPurchase.getObjectId(), isDraft());
    }
}
