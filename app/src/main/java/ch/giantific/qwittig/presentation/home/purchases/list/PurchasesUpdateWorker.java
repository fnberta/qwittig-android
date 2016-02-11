/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseQueryWorker;
import rx.Observable;
import rx.functions.Func1;

/**
 * Performs an online query to the Parse.com database to query purchases.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class PurchasesUpdateWorker extends BaseQueryWorker<Purchase, PurchasesUpdateListener> {

    private static final String WORKER_TAG = PurchasesUpdateWorker.class.getCanonicalName();
    @Inject
    PurchaseRepository mPurchaseRepo;

    public PurchasesUpdateWorker() {
        // required empty constructor
    }

    public static PurchasesUpdateWorker attach(@NonNull FragmentManager fm) {
        PurchasesUpdateWorker worker = (PurchasesUpdateWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new PurchasesUpdateWorker();
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

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Nullable
    @Override
    protected Observable<Purchase> getObservable(@NonNull Bundle args) {
        if (setCurrentGroups()) {
            mIdentityRepo.getUserIdentitiesLocalAsync(mCurrentUser)
                    .toList()
                    .flatMap(new Func1<List<Identity>, Observable<Purchase>>() {
                        @Override
                        public Observable<Purchase> call(List<Identity> identities) {
                            return mPurchaseRepo.updatePurchasesAsync(mCurrentIdentity, identities);
                        }
                    });
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Purchase> observable) {
        mActivity.setPurchasesUpdateStream(observable, WORKER_TAG);
    }
}
