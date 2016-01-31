/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import rx.Observable;

/**
 * Created by fabio on 13.01.16.
 */
public class CompensationsQueryMoreWorker extends BaseQueryWorker<Compensation, CompensationsQueryMoreListener> {

    public static final String WORKER_TAG = "COMPENSATIONS_QUERY_MORE_WORKER";
    private static final String KEY_SKIP = "SKIP";
    @Inject
    CompensationRepository mCompsRepo;

    /**
     * Return a new instance of {@link CompensationsQueryMoreWorker} with the number of items to skip
     * as arguments.
     *
     * @param skip the number of items to skip
     * @return a new instance of {@link CompensationsQueryMoreWorker}
     */
    @NonNull
    public static CompensationsQueryMoreWorker newInstance(int skip) {
        CompensationsQueryMoreWorker fragment = new CompensationsQueryMoreWorker();
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
        if (setCurrentGroups()) {
            final int skip = args.getInt(KEY_SKIP, 0);
            return mCompsRepo.getCompensationsPaidOnlineAsync(mCurrentGroup, skip);
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Compensation> observable) {
        mActivity.setCompensationsQueryMoreStream(observable, WORKER_TAG);
    }
}
