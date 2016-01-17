/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import rx.Observable;

/**
 * Performs an online query to the Parse.com database to query either paid or unpaid compensations.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class CompensationsUpdateWorker extends BaseQueryWorker<Compensation, CompensationsUpdateListener> {

    public static final String WORKER_TAG = "TASK_QUERY_WORKER";
    private static final String LOG_TAG = CompensationsUpdateWorker.class.getSimpleName();
    private static final String BUNDLE_QUERY_PAID = "BUNDLE_QUERY_PAID";
    @Inject
    CompensationRepository mCompsRepo;
    private boolean mQueryPaid;

    /**
     * Returns a new instance of {@link CompensationsUpdateWorker} with an argument whether to
     * query for paid or unpaid compensations.
     *
     * @param queryPaid whether to query paid compensations
     * @return a new instance of {@link CompensationsUpdateWorker}
     */
    @NonNull
    public static CompensationsUpdateWorker newInstance(boolean queryPaid) {
        CompensationsUpdateWorker fragment = new CompensationsUpdateWorker();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_QUERY_PAID, queryPaid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected String getWorkerTag() {
        return WORKER_TAG;
    }

    @Nullable
    @Override
    protected Observable<Compensation> getObservable(@NonNull Bundle args) {
        if (setCurrentGroups()) {
            if (mQueryPaid) {
                return mCompsRepo.updateCompensationsPaidAsync(mCurrentUserGroups, mCurrentGroup.getObjectId());
            } else {
                return mCompsRepo.updateCompensationsUnpaidAsync(mCurrentUserGroups);
            }
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Compensation> observable,
                             @NonNull String workerTag) {
        mActivity.setCompensationsUpdateStream(observable, mQueryPaid, workerTag);
    }
}
