/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.File;

import javax.inject.Inject;

import ch.giantific.qwittig.data.rest.ReceiptOcr;
import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.ocr.OcrPurchase;
import rx.Observable;
import rx.functions.Func1;

/**
 * Sends the image of receipt to the server to analyse and ocr it using
 * {@link ReceiptOcr}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class OcrWorker extends BaseWorker<OcrPurchase, OcrWorkerListener> {

    public static final String WORKER_TAG = "OCR_WORKER";
    private static final String LOG_TAG = OcrWorker.class.getSimpleName();
    private static final String BUNDLE_RECEIPT_PATH = "BUNDLE_RECEIPT_PATH";
    private static final int MAX_RETRIES = 0;
    @Inject
    ReceiptOcr mReceiptOcr;

    public OcrWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link OcrWorker} with the path to a receipt image as an argument.
     *
     * @param receiptPath the path to the image of the receipt to perform ocr on
     * @return a new instance of {@link OcrWorker}
     */
    @NonNull
    public static OcrWorker newInstance(@NonNull String receiptPath) {
        OcrWorker fragment = new OcrWorker();
        Bundle args = new Bundle();
        args.putString(BUNDLE_RECEIPT_PATH, receiptPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    protected Observable<OcrPurchase> getObservable(@NonNull Bundle args) {
        // TODO: implement retries

        final String receiptPath = args.getString(BUNDLE_RECEIPT_PATH, "");
        if (!TextUtils.isEmpty(receiptPath)) {
            return mUserRepo.getUserSessionToken()
                    .flatMapObservable(new Func1<String, Observable<OcrPurchase>>() {
                        @Override
                        public Observable<OcrPurchase> call(String sessionToken) {
                            final RequestBody tokenPart = RequestBody.create(
                                    MediaType.parse("text/plain"), sessionToken);
                            final RequestBody receiptPart = RequestBody.create(
                                    MediaType.parse("image/jpeg"), new File(receiptPath));

                            return mReceiptOcr.uploadReceipt(tokenPart, receiptPart);
                        }
                    });
        }

        return null;
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<OcrPurchase> observable) {
        mActivity.setOcrStream(observable.toSingle(), WORKER_TAG);
    }
}
