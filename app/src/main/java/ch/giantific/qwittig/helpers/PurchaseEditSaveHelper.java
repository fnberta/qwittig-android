package ch.giantific.qwittig.helpers;

import android.annotation.SuppressLint;
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
 * Created by fabio on 10.12.14.
 */
public class PurchaseEditSaveHelper extends PurchaseSaveHelper {

    private static final String LOG_TAG = PurchaseEditSaveHelper.class.getSimpleName();
    private ParseFile mReceiptParseFileOld;
    private boolean mIsDraft;

    public PurchaseEditSaveHelper() {
        // empty default constructor
    }

    @SuppressLint("ValidFragment")
    public PurchaseEditSaveHelper(ParseFile receiptParseFileOld, ParseFile receiptParseFileNew,
                                  Purchase purchase, boolean isDraft) {
        super(receiptParseFileNew, purchase);
        
        mReceiptParseFileOld = receiptParseFileOld;
        mIsDraft = isDraft;
    }

    @Override
    void checkIfReceiptNull() {
        if (mReceiptParseFile != null) {
            if (mReceiptParseFileOld != null) {
                deleteOldReceiptFile();
            } else {
                saveReceiptFile();
            }
        } else {
            if (mReceiptParseFileOld != null) {
                deleteOldReceiptFile();
                mPurchase.removeReceiptParseFile();
            } else {
                savePurchase();
            }
        }
    }

    private void deleteOldReceiptFile() {
        String fileName = mReceiptParseFileOld.getName();
        if (!TextUtils.isEmpty(fileName)) {
            deleteParseFileInCloud(fileName);
        }
    }

    private void deleteParseFileInCloud(String fileName) {
        Map<String, Object> params = new HashMap<>();
        params.put(CloudCode.PARAM_FILE_NAME, fileName);
        ParseCloud.callFunctionInBackground(CloudCode.DELETE_PARSE_FILE, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
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
        super.checkIfReceiptNull();
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
                public void done(ParseException e) {
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
