package ch.giantific.qwittig.data.queues;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

/**
 * Created by fabio on 09.08.16.
 */
@IgnoreExtraProperties
public class OcrPurchase {

    @PropertyName("receipt")
    private final String mReceipt;
    @PropertyName("purchaseId")
    private final String mPurchaseId;
    @PropertyName("userId")
    private final String mUserId;

    public OcrPurchase(@NonNull String receipt,
                       @NonNull String purchaseId,
                       @NonNull String userId) {
        mReceipt = receipt;
        mPurchaseId = purchaseId;
        mUserId = userId;
    }

    public String getReceipt() {
        return mReceipt;
    }

    public String getPurchaseId() {
        return mPurchaseId;
    }

    public String getUserId() {
        return mUserId;
    }
}
