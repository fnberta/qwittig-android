package ch.giantific.qwittig.ui;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.common.primitives.Booleans;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Map;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ItemUsersChecked;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.rates.RestClient;
import ch.giantific.qwittig.data.rates.models.CurrencyRates;
import ch.giantific.qwittig.utils.MessageUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class PurchaseAddFragment extends PurchaseBaseFragment {

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
        mPurchaseUsersInvolved.clear();
        for (ParseUser ignored : mUsersAvailableParse) {
            mPurchaseUsersInvolved.add(true);
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
        if (mItemsUsersChecked.size() == 0) {
            mItemsUsersChecked.add(new ItemUsersChecked(Booleans.toArray(mPurchaseUsersInvolved)));
        }
    }

    /**
     * Creates a new purchase Object, with or without a receipt photo and saves it to Parse and
     * pins it to local datastore. If there is no connection, it will only pin it to the local
     * datastore
     */
    @Override
    protected void setPurchase() {
        if (mCurrencySelected.equals(mCurrentGroupCurrency)) {
            createNewPurchase(1);
        } else {
            RestClient.getService().getRates(mCurrencySelected, new Callback<CurrencyRates>() {
                @Override
                public void success(CurrencyRates currencyRates, Response response) {
                    Map<String, Double> exchangeRates = currencyRates.getRates();
                    double exchangeRate = exchangeRates.get(mCurrentGroupCurrency);
                    createNewPurchase(exchangeRate);
                }

                @Override
                public void failure(RetrofitError error) {
                    savePurchaseAsDraft();
                }
            });
        }
    }

    private void createNewPurchase(double exchangeRate) {
        List<ParseUser> purchaseUsersInvolved = getParseUsersInvolvedFromBoolean(mPurchaseUsersInvolved);
        mPurchase = new Purchase(mCurrentGroup, mDateSelected, mStoreSelected,
                mItems, mTotalPrice, purchaseUsersInvolved, mCurrencySelected, exchangeRate);
        ParseFile receiptFile = mListener.getReceiptParseFile();

        if (receiptFile != null) {
            mPurchase.setReceiptParseFile(receiptFile);
            receiptFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        onParseError(e);
                        return;
                    }

                    savePurchaseInParse();
                }
            });
        } else {
            savePurchaseInParse();
        }
    }

    @Override
    void showErrorSnackbar(String message) {
        Snackbar snackbar = MessageUtils.getBasicSnackbar(mButtonAddRow, message);
        snackbar.setAction(R.string.action_purchase_save_draft, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinPurchaseAsDraft();
            }
        });
        snackbar.show();
    }

    @Override
    protected void onSaveSucceeded() {
        pinPurchase(false);
    }

    /**
     * Creates new purchase object and calls method to pin it to local datastore.
     */
    @Override
    protected void savePurchaseAsDraft() {
        List<ParseUser> purchaseUsersInvolved = getParseUsersInvolvedFromBoolean(mPurchaseUsersInvolved);
        ParseFile receiptFile = mListener.getReceiptParseFile();

        if (receiptFile != null) {
            mPurchase = new Purchase(mCurrentGroup, mDateSelected, mStoreSelected,
                    mItems, mTotalPrice, purchaseUsersInvolved, mCurrencySelected, receiptFile);
        } else {
            mPurchase = new Purchase(mCurrentGroup, mDateSelected, mStoreSelected, mItems,
                    mTotalPrice, purchaseUsersInvolved, mCurrencySelected);
        }

        pinPurchaseAsDraft();
    }
}
