package ch.giantific.qwittig.data.rxwrapper.glide.subscribers;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;

import com.bumptech.glide.Glide;

import java.util.concurrent.Callable;

import ch.giantific.qwittig.data.repositories.PurchaseRepository;

/**
 * Created by fabio on 09.08.16.
 */
public class EncodeReceiptCallable implements Callable<String> {

    private final String receiptPath;
    private final FragmentActivity activity;

    public EncodeReceiptCallable(@NonNull String receiptPath,
                                 @NonNull FragmentActivity activity) {
        this.receiptPath = receiptPath;
        this.activity = activity;
    }

    @Override
    public String call() throws Exception {
        final byte[] bytes = Glide.with(activity)
                .load(receiptPath)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, PurchaseRepository.JPEG_COMPRESSION_RATE)
                .into(PurchaseRepository.WIDTH, PurchaseRepository.HEIGHT)
                .get();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
