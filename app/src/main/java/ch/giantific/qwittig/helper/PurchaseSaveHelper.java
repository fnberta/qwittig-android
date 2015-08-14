package ch.giantific.qwittig.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Item;
import ch.giantific.qwittig.data.parse.models.Purchase;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 10.12.14.
 */
public class PurchaseSaveHelper extends BaseHelper {

    private static final String LOG_TAG = PurchaseSaveHelper.class.getSimpleName();
    HelperInteractionListener mListener;
    Purchase mPurchase;
    ParseFile mReceiptParseFile;

    public PurchaseSaveHelper() {
        // empty default constructor
    }

    @SuppressLint("ValidFragment")
    public PurchaseSaveHelper(ParseFile receiptParseFile, Purchase purchase) {
        mReceiptParseFile = receiptParseFile;
        mPurchase = purchase;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkIfReceiptNull();
    }

    void checkIfReceiptNull() {
        if (mReceiptParseFile != null) {
            saveReceiptFile();
        } else {
            savePurchase();
        }
    }

    final void saveReceiptFile() {
        mPurchase.setReceiptParseFile(mReceiptParseFile);
        mReceiptParseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null && mListener != null) {
                    mListener.onPurchaseSaveFailed(e);
                    return;
                }

                onReceiptFileSaved();
            }
        });
    }

    void onReceiptFileSaved() {
        savePurchase();
    }

    final void savePurchase() {
        convertPrices(true);
        mPurchase.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    convertPrices(false);

                    if (mListener != null) {
                        mListener.onPurchaseSaveFailed(e);
                    }
                    return;
                }

                onPurchaseSaved();
            }
        });
    }

    private void convertPrices(boolean toGroupCurrency) {
        float exchangeRate = mPurchase.getExchangeRate();
        if (exchangeRate == 1) {
            return;
        }

        List<ParseObject> items = mPurchase.getItems();
        for (ParseObject parseObject : items) {
            Item item = (Item) parseObject;
            item.convertPrice(exchangeRate, toGroupCurrency);
        }

        mPurchase.convertTotalPrice(toGroupCurrency);
    }

    void onPurchaseSaved() {
        pinPurchase();
    }

    /**
     * Pins purchase to local datastore.
     */
    final void pinPurchase() {
        final User currentUser = (User) ParseUser.getCurrentUser();
        Group currentGroup = currentUser.getCurrentGroup();

        mPurchase.pinInBackground(Purchase.PIN_LABEL + currentGroup.getObjectId(), new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null && mListener != null) {
                        mListener.onPurchaseSaveAndPinSucceeded();
                    }
                }
            });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onPurchaseSaveAndPinSucceeded();

        void onPurchaseSaveFailed(ParseException e);
    }
}
