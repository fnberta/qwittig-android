/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import javax.inject.Inject;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.domain.models.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseQueryWorker;
import rx.Observable;

/**
 * Created by fabio on 13.01.16.
 */
public class PurchasesQueryMoreWorker extends BaseQueryWorker<Purchase, PurchasesQueryMoreWorkerListener> {

    private static final String WORKER_TAG = PurchasesQueryMoreWorker.class.getCanonicalName();
    private static final String KEY_SKIP = "SKIP";
    @Inject
    PurchaseRepository mPurchaseRepo;

    /**
     * Attaches a new instance of {@link PurchasesQueryMoreWorker} with the number of items to skip
     * as arguments.
     *
     * @param fm   the fragment manger to use for the transaction
     * @param skip the number of items to skip
     * @return a new instance of {@link PurchasesQueryMoreWorker}
     */
    public static PurchasesQueryMoreWorker attach(@NonNull FragmentManager fm, int skip) {
        PurchasesQueryMoreWorker worker = (PurchasesQueryMoreWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = PurchasesQueryMoreWorker.newInstance(skip);
            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    private static PurchasesQueryMoreWorker newInstance(int skip) {
        PurchasesQueryMoreWorker fragment = new PurchasesQueryMoreWorker();
        Bundle args = new Bundle();
        args.putInt(KEY_SKIP, skip);
        fragment.setArguments(args);
        return fragment;
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
        if (setUserInfo()) {
            final int skip = args.getInt(KEY_SKIP, 0);
            return mPurchaseRepo.getPurchasesOnlineAsync(mCurrentIdentity, skip);
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Purchase> observable) {
        mActivity.setPurchasesQueryMoreStream(observable, WORKER_TAG);
    }
}
