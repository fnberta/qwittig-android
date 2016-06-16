package ch.giantific.qwittig.data.bus.events;

import android.support.annotation.NonNull;

/**
 * Created by fabio on 15.06.16.
 */
public class EventReceiptImageTaken {

    @NonNull
    private final String mReceiptImagePath;

    public EventReceiptImageTaken(@NonNull String receiptImagePath) {

        mReceiptImagePath = receiptImagePath;
    }

    @NonNull
    public String getReceiptImagePath() {
        return mReceiptImagePath;
    }
}
