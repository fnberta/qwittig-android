package ch.giantific.qwittig.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import ch.giantific.qwittig.data.models.Receipt;
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
    String mReceiptPath;

    public PurchaseSaveHelper() {
        // empty default constructor
    }

    @SuppressLint("ValidFragment")
    public PurchaseSaveHelper(Purchase purchase, String receiptPath) {
        mPurchase = purchase;
        mReceiptPath = receiptPath;
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

        checkReceiptImage();
    }

    void checkReceiptImage() {
        if (!TextUtils.isEmpty(mReceiptPath)) {
            saveReceiptFile();
        } else {
            savePurchase();
        }
    }

    final void saveReceiptFile() {
        Glide.with(this).load(mReceiptPath)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, Receipt.JPEG_COMPRESSION_RATE)
                .centerCrop()
                .into(new SimpleTarget<byte[]>(Receipt.WIDTH, Receipt.HEIGHT) {
                    @Override
                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                        ParseFile file = new ParseFile(Receipt.PARSE_FILE_NAME, resource);
                        mPurchase.setReceiptParseFile(file);

                        file.saveInBackground(new SaveCallback() {
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
                        mListener.onPurchaseSavedAndPinned();
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
        void onPurchaseSavedAndPinned();

        void onPurchaseSaveFailed(ParseException e);
    }
}
