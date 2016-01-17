/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Purchase;
import ch.giantific.qwittig.domain.repositories.PurchaseRepository;
import rx.Observable;

/**
 * Created by fabio on 13.01.16.
 */
public class PurchasesQueryMoreWorker extends BaseQueryWorker<Purchase, PurchasesQueryMoreListener> {

    public static final String WORKER_TAG = "PURCHASES_QUERY_MORE_WORKER";
    private static final String BUNDLE_SKIP = "BUNDLE_SKIP";
    @Inject
    PurchaseRepository mPurchaseRepo;

    /**
     * Return a new instance of {@link PurchasesQueryMoreWorker} with the number of items to skip
     * as arguments.
     *
     * @param skip the number of items to skip
     * @return a new instance of {@link PurchasesQueryMoreWorker}
     */
    @NonNull
    public static PurchasesQueryMoreWorker newInstance(int skip) {
        PurchasesQueryMoreWorker fragment = new PurchasesQueryMoreWorker();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_SKIP, skip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected String getWorkerTag() {
        return WORKER_TAG;
    }

    @Nullable
    @Override
    protected Observable<Purchase> getObservable(@NonNull Bundle args) {
        if (setCurrentGroups()) {
            final int skip = args.getInt(BUNDLE_SKIP, 0);
            return mPurchaseRepo.getPurchasesOnlineAsync(mCurrentUser, mCurrentGroup, skip);
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Purchase> observable, @NonNull String workerTag) {
        mActivity.setPurchasesQueryMoreStream(observable, workerTag);
    }
}
