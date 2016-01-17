/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.workerfragments.save.PurchaseSaveWorker;
import ch.giantific.qwittig.domain.models.ItemRow;
import ch.giantific.qwittig.domain.models.parse.Purchase;

/**
 * Displays the interface where the user can add a new purchase by setting store, date, users
 * involved and the different items.
 * <p/>
 * Subclass of {@link PurchaseBaseFragment}.
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
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_purchase_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
    void setupUserLists(@NonNull List<ParseUser> users) {
        super.setupUserLists(users);

        setFirstRowItemUsersChecked();
    }

    /**
     * On first start, mItemsUsersChecked for the first automatically created row will be empty,
     * fill it with default purchase wide usersInvolved. On recreation, mItemsUsersChecked will be
     * not be empty, hence the item's values will not be reset.
     */
    void setFirstRowItemUsersChecked() {
        ItemRow firstItemRow = mItemRows.get(0);
        if (mItemRows.size() == 1 && firstItemRow.getUsersChecked() == null) {
            firstItemRow.setUsersChecked(Arrays.copyOf(mPurchaseUsersInvolved,
                    mPurchaseUsersInvolved.length));
        }
    }

    @Override
    public void deleteReceipt() {
        deleteTakenImages();
        if (USE_CUSTOM_CAMERA) {
            mReceiptImagePaths.clear();
        }
    }

    /**
     * Creates a new {@link Purchase}, with or without a receipt photo, saves it to the Parse.com
     * online data base and pins it to local data store.
     */
    @Override
    protected void setPurchase() {
        List<ParseUser> purchaseUsersInvolved = getParseUsersInvolvedFromBoolean(mPurchaseUsersInvolved);
        mPurchase = new Purchase(mCurrentUser, mCurrentGroup, mDateSelected, mStoreSelected,
                mItems, mTotalPrice, purchaseUsersInvolved, mCurrencySelected, mExchangeRate);
        if (!TextUtils.isEmpty(mNote)) {
            mPurchase.setNote(mNote);
        }
    }

    @Override
    protected PurchaseSaveWorker getSaveWorker() {
        return new PurchaseSaveWorker(mPurchase, mReceiptImagePath);
    }

    @Override
    void showErrorSnackbar(@StringRes int message) {
        Snackbar snackbar = Snackbar.make(mButtonAddRow, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.action_purchase_save_draft, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPurchase != null) {
                    ParseFile receipt = mPurchase.getReceiptParseFile();
                    if (receipt != null) {
                        receipt.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(@NonNull byte[] data, ParseException e) {
                                mPurchase.swapReceiptParseFileToData(data);
                                pinPurchaseAsDraft();
                            }
                        });
                    } else {
                        pinPurchaseAsDraft();
                    }
                } else {
                    // if we failed fast because of no network, purchase will not yet be created
                    savePurchaseAsDraft();
                }
            }
        });
        snackbar.show();
    }

    @Override
    protected void savePurchaseAsDraft() {
        List<ParseUser> purchaseUsersInvolved = getParseUsersInvolvedFromBoolean(mPurchaseUsersInvolved);
        mPurchase = new Purchase(mCurrentUser, mCurrentGroup, mDateSelected, mStoreSelected, mItems,
                mTotalPrice, purchaseUsersInvolved, mCurrencySelected);

        if (TextUtils.isEmpty(mReceiptImagePath)) {
            pinPurchaseAsDraft();
        } else {
            getReceiptDataForDraft();
        }
    }

    @Override
    protected PurchaseReceiptBaseFragment getReceiptFragment() {
        return PurchaseReceiptAddFragment.newInstance(mReceiptImagePath);
    }
}
