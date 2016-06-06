/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import javax.inject.Inject;

import ch.giantific.qwittig.data.rest.ReceiptOcr;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;
import rx.functions.Func1;

/**
 * Sends the image of receipt to the server to analyse and ocr it using
 * {@link ReceiptOcr}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class OcrWorker extends BaseWorker<String, OcrWorkerListener> {

    private static final String WORKER_TAG = OcrWorker.class.getCanonicalName();
    private static final String KEY_RECEIPT_PATH = "RECEIPT_PATH";
    @Inject
    PurchaseRepository mPurchaseRepo;

    public OcrWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link OcrWorker} with the path to a receipt image as an argument.
     *
     * @param fm          the fragment manager to use for the transaction
     * @param receiptPath the path to the image of the receipt to perform ocr on
     * @return a new instance of {@link OcrWorker}
     */
    public static OcrWorker attach(@NonNull FragmentManager fm, @NonNull String receiptPath) {
        OcrWorker worker = (OcrWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new OcrWorker();
            final Bundle args = new Bundle();
            args.putString(KEY_RECEIPT_PATH, receiptPath);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    protected Observable<String> getObservable(@NonNull Bundle args) {
        final String receiptPath = args.getString(KEY_RECEIPT_PATH, "");
        if (!TextUtils.isEmpty(receiptPath)) {
            return mUserRepo.getUserSessionToken()
                    .flatMapObservable(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String sessionToken) {
                            return mPurchaseRepo.uploadReceipt(sessionToken, receiptPath);
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
    protected void setStream(@NonNull Observable<String> observable) {
        mActivity.setOcrStream(observable.toSingle(), WORKER_TAG);
    }
}
