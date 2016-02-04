/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import rx.Observable;
import rx.functions.Func1;

/**
 * Performs an online query to the Parse.com database to query either paid or unpaid compensations.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class CompensationsUpdateWorker extends BaseQueryWorker<Compensation, CompensationsUpdateListener> {

    private static final String WORKER_TAG = CompensationsUpdateWorker.class.getCanonicalName();
    private static final String KEY_QUERY_PAID = "QUERY_PAID";
    @Inject
    CompensationRepository mCompsRepo;
    private boolean mQueryPaid;

    /**
     * Attaches a new instance of {@link CompensationsUpdateWorker} with an argument whether to
     * query for paid or unpaid compensations.
     *
     * @param fm        the fragment manager to use for the transaction
     * @param queryPaid whether to query paid compensations
     * @return a new instance of {@link CompensationsUpdateWorker}
     */
    public static CompensationsUpdateWorker attach(@NonNull FragmentManager fm, boolean queryPaid) {
        CompensationsUpdateWorker worker = (CompensationsUpdateWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = CompensationsUpdateWorker.newInstance(queryPaid);
            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    @NonNull
    public static CompensationsUpdateWorker newInstance(boolean queryPaid) {
        CompensationsUpdateWorker fragment = new CompensationsUpdateWorker();
        Bundle args = new Bundle();
        args.putBoolean(KEY_QUERY_PAID, queryPaid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    protected Observable<Compensation> getObservable(@NonNull Bundle args) {
        mQueryPaid = args.getBoolean(KEY_QUERY_PAID);

        if (setCurrentGroups()) {
            if (mQueryPaid) {
                return mCompsRepo.updateCompensationsPaidAsync(mCurrentUserGroups, mCurrentGroup.getObjectId());
            } else {
                return mUserRepo.updateUsersAsync(mCurrentUserGroups)
                        .flatMap(new Func1<User, Observable<Compensation>>() {
                            @Override
                            public Observable<Compensation> call(User user) {
                                return mCompsRepo.updateCompensationsUnpaidAsync(mCurrentUserGroups);
                            }
                        });
            }
        }

        return null;
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<Compensation> observable) {
        mActivity.setCompensationsUpdateStream(observable, mQueryPaid, WORKER_TAG);
    }
}
