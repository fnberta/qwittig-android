/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.parse.ParseFile;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Saves a {@link Purchase} object and its corresponding receipt image as a {@link ParseFile}.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class PurchaseSaveWorker extends BaseWorker<Purchase, PurchaseSaveWorkerListener> {

    static final String WORKER_TAG = PurchaseSaveWorker.class.getCanonicalName();
    @Inject
    PurchaseRepository mPurchaseRepo;
    Purchase mPurchase;
    byte[] mReceiptImage;

    public PurchaseSaveWorker() {
        // empty default constructor
    }

    /**
     * Constructs a new {@link PurchaseSaveWorker} with a {@link Purchase} object and optionally
     * the path to the receipt image as parameters.
     * <p/>
     * Using a non empty constructor to be able to pass a {@link com.parse.ParseObject}.
     * Because the fragment  is retained across configuration changes, there is no risk that the
     * system will recreate it with the default empty constructor.
     *
     * @param purchase     the {@link Purchase} object to save
     * @param receiptImage the receipt image to attach to the purchase
     */
    @SuppressLint("ValidFragment")
    PurchaseSaveWorker(@NonNull Purchase purchase, @Nullable byte[] receiptImage) {
        mPurchase = purchase;
        mReceiptImage = receiptImage;
    }

    /**
     * Attaches a new instance of a {@link PurchaseSaveWorker}.
     *
     * @param fm           the fragment manager to use for the transaction
     * @param purchase     the {@link Purchase} object to save
     * @param receiptImage the receipt image to attach to the purchase
     * @return a new instance of a {@link PurchaseSaveWorker}
     */
    public static PurchaseSaveWorker attach(@NonNull FragmentManager fm,
                                            @NonNull Purchase purchase,
                                            @Nullable byte[] receiptImage) {
        PurchaseSaveWorker worker = (PurchaseSaveWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new PurchaseSaveWorker(purchase, receiptImage);

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
    protected Observable<Purchase> getObservable(@NonNull Bundle args) {
        final User currentUser = mUserRepo.getCurrentUser();
        if (currentUser != null) {
            final Identity currentIdentity = currentUser.getCurrentIdentity();
            final String tag = Purchase.PIN_LABEL + currentIdentity.getGroup().getObjectId();
            return getPurchaseObservable(tag);
        }

        return null;
    }

    @NonNull
    Observable<Purchase> getPurchaseObservable(@NonNull String tag) {
        return mPurchaseRepo.savePurchaseAsync(mPurchase, tag, mReceiptImage, false).toObservable();
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<Purchase> observable) {
        mActivity.setPurchaseSaveStream(observable.toSingle(), WORKER_TAG);
    }
}
