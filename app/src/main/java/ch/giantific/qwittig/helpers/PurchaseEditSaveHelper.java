/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.DeleteCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Purchase;

/**
 * Saves an edited {@link Purchase} object and if there is a new receipt image, deletes the old
 * {@link ParseFile} if there was one.
 * <p/>
 * Subclass of {@link PurchaseSaveHelper}.
 */
public class PurchaseEditSaveHelper extends PurchaseSaveHelper {

    private static final String LOG_TAG = PurchaseEditSaveHelper.class.getSimpleName();
    @Nullable
    private ParseFile mReceiptParseFileOld;
    private boolean mIsDraft;
    private boolean mDeleteOldReceipt;

    public PurchaseEditSaveHelper() {
        // empty default constructor
    }

    /**
     * Constructs a new {@link PurchaseEditSaveHelper} with a {@link Purchase} object, whether
     * the purchase was a draft or an already saved purchase and optionally the old receipt image
     * as parameters.
     * <p/>
     * Using a non empty constructor to be able to pass a {@link com.parse.ParseObject}.
     * Because the fragment  is retained across configuration changes, there is no risk that the
     * system will recreate it with the default empty constructor.
     *
     * @param purchase            the {@link Purchase} object to save
     * @param isDraft             whether we are saving a draft or an already saved purchase
     * @param receiptParseFileOld the old receipt image
     */
    @SuppressLint("ValidFragment")
    public PurchaseEditSaveHelper(@NonNull Purchase purchase, boolean isDraft,
                                  @Nullable ParseFile receiptParseFileOld, @NonNull String note) {
        this(purchase, isDraft, receiptParseFileOld, "", note);

        mDeleteOldReceipt = true;
    }

    /**
     * Constructs a new {@link PurchaseEditSaveHelper} with a {@link Purchase} object, whether
     * the purchase was a draft or an already saved purchase and optionally the old receipt image
     * and the path to the new one as parameters.
     * <p/>
     * Using a non empty constructor to be able to pass a {@link com.parse.ParseObject}.
     * Because the fragment  is retained across configuration changes, there is no risk that the
     * system will recreate it with the default empty constructor.
     *
     * @param purchase            the {@link Purchase} object to save
     * @param isDraft             whether we are saving a draft or an already saved purchase
     * @param receiptParseFileOld the old receipt image
     * @param receiptNewPath      the path to the new receipt image
     */
    @SuppressLint("ValidFragment")
    public PurchaseEditSaveHelper(@NonNull Purchase purchase, boolean isDraft,
                                  @Nullable ParseFile receiptParseFileOld,
                                  @Nullable String receiptNewPath, @NonNull String note) {
        super(purchase, receiptNewPath, note);

        mReceiptParseFileOld = receiptParseFileOld;
        mIsDraft = isDraft;
    }

    @Override
    void checkReceiptImage() {
        if (!TextUtils.isEmpty(mReceiptPath)) {
            if (mReceiptParseFileOld != null) {
                deleteOldReceiptFile();
            } else {
                saveReceiptFile();
            }
        } else if (mDeleteOldReceipt && mReceiptParseFileOld != null) {
            deleteOldReceiptFile();
            mPurchase.removeReceiptParseFile();
        } else {
            savePurchase();
        }
    }

    private void deleteOldReceiptFile() {
        String fileName = mReceiptParseFileOld.getName();
        if (!TextUtils.isEmpty(fileName)) {
            deleteParseFileInCloud(fileName);
        }
    }

    private void deleteParseFileInCloud(@NonNull String fileName) {
        Map<String, Object> params = new HashMap<>();
        params.put(CloudCode.PARAM_FILE_NAME, fileName);
        ParseCloud.callFunctionInBackground(CloudCode.DELETE_PARSE_FILE, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onPurchaseSaveFailed(e);
                    }
                    return;
                }

                onOldReceiptParseFileDeleted();
            }
        });
    }

    private void onOldReceiptParseFileDeleted() {
        super.checkReceiptImage();
    }

    @Override
    void onReceiptFileSaved() {
        if (mIsDraft) {
            mPurchase.removeReceiptData();
        }

        super.onReceiptFileSaved();
    }

    @Override
    void onPurchaseSaved() {
        if (mIsDraft) {
            mPurchase.unpinInBackground(new DeleteCallback() {
                @Override
                public void done(@Nullable ParseException e) {
                    if (e != null) {
                        return;
                    }

                    pinPurchase();
                }
            });
        } else {
            super.onPurchaseSaved();
        }
    }
}
