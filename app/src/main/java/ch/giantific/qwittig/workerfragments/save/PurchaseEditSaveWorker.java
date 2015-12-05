/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.save;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.domain.models.parse.Purchase;

/**
 * Saves an edited {@link Purchase} object and if there is a new receipt image, deletes the old
 * {@link ParseFile} if there was one.
 * <p/>
 * Subclass of {@link PurchaseSaveWorker}.
 */
public class PurchaseEditSaveWorker extends PurchaseSaveWorker implements
        CloudCodeClient.CloudCodeListener {

    private static final String LOG_TAG = PurchaseEditSaveWorker.class.getSimpleName();
    @Nullable
    private ParseFile mReceiptParseFileOld;
    private boolean mIsDraft;
    private boolean mDeleteOldReceipt;
    private CloudCodeClient mCloudClient;

    public PurchaseEditSaveWorker() {
        // empty default constructor
    }

    /**
     * Constructs a new {@link PurchaseEditSaveWorker} with a {@link Purchase} object, whether
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
    public PurchaseEditSaveWorker(@NonNull Purchase purchase, boolean isDraft,
                                  @Nullable ParseFile receiptParseFileOld, boolean deleteOldReceipt,
                                  @Nullable String receiptNewPath) {
        super(purchase, receiptNewPath);

        mDeleteOldReceipt = deleteOldReceipt;
        mReceiptParseFileOld = receiptParseFileOld;
        mIsDraft = isDraft;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCloudClient = new CloudCodeClient(getActivity());
    }

    @Override
    void checkReceiptImage() {
        if (!TextUtils.isEmpty(mReceiptPath)) {
            if (mReceiptParseFileOld != null && !mIsDraft) {
                deleteOldReceiptFile();
            } else {
                getReceiptFile();
            }
        } else if (mReceiptParseFileOld != null) {
            if (mIsDraft) {
                saveReceiptParseFile(mReceiptParseFileOld);
            } else if (mDeleteOldReceipt) {
                deleteOldReceiptFile();
                mPurchase.removeReceiptParseFile();
            } else {
                savePurchase();
            }
        } else {
            savePurchase();
        }
    }

    private void deleteOldReceiptFile() {
        final String fileName = mReceiptParseFileOld.getName();
        if (!TextUtils.isEmpty(fileName)) {
            mCloudClient.deleteParseFile(fileName, this);
        }
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        super.checkReceiptImage();
    }

    @Override
    public void onCloudFunctionFailed(@StringRes int errorMessage) {
        if (mListener != null) {
            mListener.onPurchaseSaveFailed(errorMessage);
        }
    }

    @Override
    void onReceiptFileSaved(ParseFile receipt) {
        if (mIsDraft) {
            mPurchase.removeReceiptData();
        }

        super.onReceiptFileSaved(receipt);
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
