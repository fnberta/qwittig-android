/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import rx.Observable;

/**
 * Performs an online query to the Parse.com database to query purchases.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class PurchasesUpdateWorker extends BaseQueryWorker<Purchase, PurchasesUpdateListener> {

    public static final String WORKER_TAG = "PURCHASE_UPDATE_WORKER";
    private static final String LOG_TAG = PurchasesUpdateWorker.class.getSimpleName();
    @Inject
    PurchaseRepository mPurchaseRepo;

    public PurchasesUpdateWorker() {
        // required empty constructor
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Nullable
    @Override
    protected Observable<Purchase> getObservable(@NonNull Bundle args) {
        if (setCurrentGroups()) {
            return mPurchaseRepo.updatePurchasesAsync(mCurrentUser, mCurrentUserGroups, mCurrentGroup.getObjectId());
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Purchase> observable) {
        mActivity.setPurchasesUpdateStream(observable, WORKER_TAG);
    }
}
