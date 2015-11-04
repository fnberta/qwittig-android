/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.save;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import ch.giantific.qwittig.domain.models.Receipt;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Item;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.helpers.BaseHelper;

/**
 * Saves a {@link Purchase} object and its corresponding receipt image as a {@link ParseFile}.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class PurchaseSaveHelper extends BaseHelper {

    private static final String LOG_TAG = PurchaseSaveHelper.class.getSimpleName();
    @Nullable
    HelperInteractionListener mListener;
    Purchase mPurchase;
    @Nullable
    String mReceiptPath;

    public PurchaseSaveHelper() {
        // empty default constructor
    }

    /**
     * Constructs a new {@link PurchaseSaveHelper} with a {@link Purchase} object and optionally
     * the path to the receipt image as parameters.
     * <p/>
     * Using a non empty constructor to be able to pass a {@link com.parse.ParseObject}.
     * Because the fragment  is retained across configuration changes, there is no risk that the
     * system will recreate it with the default empty constructor.
     *
     * @param purchase    the {@link Purchase} object to save
     * @param receiptPath the path to the receipt image
     */
    @SuppressLint("ValidFragment")
    public PurchaseSaveHelper(@NonNull Purchase purchase, @Nullable String receiptPath) {
        mPurchase = purchase;
        mReceiptPath = receiptPath;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
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
                    public void onResourceReady(@NonNull byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                        ParseFile file = new ParseFile(Receipt.PARSE_FILE_NAME, resource);
                        mPurchase.setReceiptParseFile(file);

                        file.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(@Nullable ParseException e) {
                                if (e != null && mListener != null) {
                                    mListener.onPurchaseSaveFailed(e.getCode());
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
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    convertPrices(false);

                    if (mListener != null) {
                        mListener.onPurchaseSaveFailed(e.getCode());
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

    final void pinPurchase() {
        final User currentUser = (User) ParseUser.getCurrentUser();
        final Group currentGroup = currentUser.getCurrentGroup();

        mPurchase.pinInBackground(Purchase.PIN_LABEL + currentGroup.getObjectId(), new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
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

    /**
     * Defines the action to take after the purchases are saved and pinned to the local data store
     * or after the save process failed.
     */
    public interface HelperInteractionListener {
        /**
         * Handles the successful save and pin to the local data store.
         */
        void onPurchaseSavedAndPinned();

        /**
         * Handles the failed save or pin to the local data store.
         *
         * @param errorCode the error code of the exception thrown in the process
         */
        void onPurchaseSaveFailed(int errorCode);
    }
}
