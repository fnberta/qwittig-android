/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.domain.models.Compensation;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.repositories.CompensationRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseQueryWorker;
import rx.Observable;
import rx.functions.Func1;

/**
 * Performs an online query to the Parse.com database to query either paid or unpaid compensations.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class CompsUpdateWorker extends BaseQueryWorker<Compensation, CompsUpdateWorkerListener> {

    private static final String WORKER_TAG = CompsUpdateWorker.class.getCanonicalName();
    private static final String KEY_QUERY_PAID = "QUERY_PAID";
    @Inject
    CompensationRepository mCompsRepo;
    private boolean mQueryPaid;

    /**
     * Attaches a new instance of {@link CompsUpdateWorker} with an argument whether to
     * query for paid or unpaid compensations.
     *
     * @param fm        the fragment manager to use for the transaction
     * @param queryPaid whether to query paid compensations
     * @return a new instance of {@link CompsUpdateWorker}
     */
    public static CompsUpdateWorker attach(@NonNull FragmentManager fm, boolean queryPaid) {
        CompsUpdateWorker worker = (CompsUpdateWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = CompsUpdateWorker.newInstance(queryPaid);
            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    @NonNull
    private static CompsUpdateWorker newInstance(boolean queryPaid) {
        CompsUpdateWorker fragment = new CompsUpdateWorker();
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

        if (setUserInfo()) {
            return mIdentityRepo.updateIdentitiesAsync(mIdentities)
                    .toList()
                    .flatMap(new Func1<List<Identity>, Observable<Compensation>>() {
                        @Override
                        public Observable<Compensation> call(List<Identity> identities) {
                            if (mQueryPaid) {
                                return mCompsRepo.updateCompensationsPaidAsync(mCurrentIdentity, mIdentities);
                            } else {
                                return mCompsRepo.calculateCompensations(mCurrentIdentity.getGroup())
                                        .flatMapObservable(new Func1<String, Observable<? extends Compensation>>() {
                                            @Override
                                            public Observable<? extends Compensation> call(String s) {
                                                return mCompsRepo.updateCompensationsUnpaidAsync(mIdentities);
                                            }
                                        });
                            }
                        }
                    });
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
