/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.identities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.workers.BaseQueryWorker;
import rx.Observable;

/**
 * Performs an online query to the Parse.com database to query users.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class IdentitiesUpdateWorker extends BaseQueryWorker<Identity, IdentitiesUpdateWorkerListener> {

    private static final String WORKER_TAG = IdentitiesUpdateWorker.class.getCanonicalName();

    public IdentitiesUpdateWorker() {
        // required empty constructor
    }

    /**
     * Attaches a new instance of a {@link IdentitiesUpdateWorker}.
     *
     * @param fm the fragment manager to use for the transaction
     * @return a new instance of a {@link IdentitiesUpdateWorker}
     */
    public static IdentitiesUpdateWorker attach(@NonNull FragmentManager fm) {
        IdentitiesUpdateWorker worker = (IdentitiesUpdateWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new IdentitiesUpdateWorker();
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
    protected Observable<Identity> getObservable(@NonNull Bundle args) {
        if (checkIdentities()) {
            return mIdentityRepo.updateIdentitiesAsync(mIdentities);
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Identity> observable) {
        mActivity.setIdentitiesUpdateStream(observable, WORKER_TAG);
    }
}
