/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseQueryWorker;
import rx.Observable;

/**
 * Created by fabio on 13.01.16.
 */
public class CompsQueryMoreWorker extends BaseQueryWorker<Compensation, CompsQueryMoreWorkerListener> {

    private static final String WORKER_TAG = CompsQueryMoreWorker.class.getCanonicalName();
    private static final String KEY_SKIP = "SKIP";
    @Inject
    CompensationRepository mCompsRepo;

    /**
     * Attaches a new instance of {@link CompsQueryMoreWorker} with the number of items to
     * skip as arguments.
     *
     * @param fm   the fragment manager to use for the transaction
     * @param skip the number of items to skip
     * @return a new instance of {@link CompsQueryMoreWorker}
     */
    public static CompsQueryMoreWorker attach(@NonNull FragmentManager fm, int skip) {
        CompsQueryMoreWorker worker = (CompsQueryMoreWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = CompsQueryMoreWorker.newInstance(skip);
            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    @NonNull
    private static CompsQueryMoreWorker newInstance(int skip) {
        CompsQueryMoreWorker fragment = new CompsQueryMoreWorker();
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
    protected Observable<Compensation> getObservable(@NonNull Bundle args) {
        if (setUserInfo()) {
            final int skip = args.getInt(KEY_SKIP, 0);
            return mCompsRepo.getCompensationsPaidOnlineAsync(mCurrentIdentity, skip);
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Compensation> observable) {
        mActivity.setCompensationsQueryMoreStream(observable, WORKER_TAG);
    }
}
