package ch.giantific.qwittig.utils.rxwrapper.glide;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import ch.giantific.qwittig.utils.rxwrapper.glide.subscribers.EncodeReceiptCallable;
import rx.Single;

/**
 * Created by fabio on 09.08.16.
 */
public class RxGlide {

    @NonNull
    public static Single<String> encodeReceipt(@NonNull final String receiptPath,
                                               @NonNull final FragmentActivity activity) {
        return Single.fromCallable(new EncodeReceiptCallable(receiptPath, activity));
    }
}
