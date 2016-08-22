package ch.giantific.qwittig.data.queues;

import android.support.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by fabio on 09.08.16.
 */
@IgnoreExtraProperties
public class OcrQueue {

    private final String receipt;
    private final String purchaseId;
    private final String userId;

    public OcrQueue(@NonNull String receipt,
                    @NonNull String purchaseId,
                    @NonNull String userId) {
        this.receipt = receipt;
        this.purchaseId = purchaseId;
        this.userId = userId;
    }

    public String getReceipt() {
        return receipt;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public String getUserId() {
        return userId;
    }
}
