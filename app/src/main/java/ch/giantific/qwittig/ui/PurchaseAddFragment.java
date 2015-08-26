package ch.giantific.qwittig.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ItemRow;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.helpers.PurchaseSaveHelper;
import ch.giantific.qwittig.utils.MessageUtils;


/**
 * A placeholder fragment containing a simple view.
 */
public class PurchaseAddFragment extends PurchaseBaseFragment {

    private static final String LOG_TAG = PurchaseAddFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchUsersAvailable();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_purchase_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_purchase_save_draft:
                savePurchase(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void setupPurchaseUsersInvolved() {
        int usersAvailableParseSize = mUsersAvailableParse.size();
        mPurchaseUsersInvolved = new boolean[usersAvailableParseSize];
        for (int i = 0; i < usersAvailableParseSize; i++) {
            mPurchaseUsersInvolved[i] = true;
        }
    }

    @Override
    void setupUserLists(List<ParseUser> users) {
        super.setupUserLists(users);

        setFirstRowItemUsersChecked();
        updateCheckBoxesColor();
    }

    /**
     * On first start, mItemsUsersChecked for the first automatically created row will be empty,
     * fill it with default purchase wide usersInvolved. On recreation, mItemsUsersChecked will be
     * not be empty, hence the item's values will not be reset.
     */
    private void setFirstRowItemUsersChecked() {
        ItemRow firstItemRow = mItemRows.get(0);
        if (mItemRows.size() == 1 && firstItemRow.getUsersChecked() == null) {
            firstItemRow.setUsersChecked(mPurchaseUsersInvolved);
        }
    }

    /**
     * Creates a new purchase Object, with or without a receipt photo and saves it to Parse and
     * pins it to local datastore. If there is no connection, it will only pin it to the local
     * datastore
     */
    @Override
    protected void setPurchase() {
        createNewPurchase();
    }

    private void createNewPurchase() {
        List<ParseUser> purchaseUsersInvolved = getParseUsersInvolvedFromBoolean(mPurchaseUsersInvolved);
        mPurchase = new Purchase(mCurrentGroup, mDateSelected, mStoreSelected,
                mItems, mTotalPrice, purchaseUsersInvolved, mCurrencySelected, mExchangeRate);
        ParseFile receiptFile = mListener.getReceiptParseFile();

        savePurchaseWithHelper(receiptFile);
    }

    private void savePurchaseWithHelper(ParseFile receiptFile) {
        FragmentManager fragmentManager = getFragmentManager();
        PurchaseSaveHelper purchaseSaveHelper = findPurchaseSaveHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (purchaseSaveHelper == null) {
            purchaseSaveHelper = new PurchaseSaveHelper(receiptFile, mPurchase);

            fragmentManager.beginTransaction()
                    .add(purchaseSaveHelper, PURCHASE_SAVE_HELPER)
                    .commit();
        }
    }

    @Override
    void showErrorSnackbar(String message) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mButtonAddRow, message);
        snackbar.setAction(R.string.action_purchase_save_draft, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPurchase != null) {
                    pinPurchaseAsDraft();
                } else {
                    // if we failed fast because of no network, purchase will not yet be created
                    savePurchaseAsDraft();
                }
            }
        });
        snackbar.show();
    }

    /**
     * Creates new purchase object and calls method to pin it to local datastore.
     */
    @Override
    protected void savePurchaseAsDraft() {
        List<ParseUser> purchaseUsersInvolved = getParseUsersInvolvedFromBoolean(mPurchaseUsersInvolved);
        mPurchase = new Purchase(mCurrentGroup, mDateSelected, mStoreSelected, mItems,
                mTotalPrice, purchaseUsersInvolved, mCurrencySelected);

        ParseFile receiptFile = mListener.getReceiptParseFile();
        if (receiptFile != null) {
            mPurchase.setReceiptParseFile(receiptFile);
        }

        pinPurchaseAsDraft();
    }
}
