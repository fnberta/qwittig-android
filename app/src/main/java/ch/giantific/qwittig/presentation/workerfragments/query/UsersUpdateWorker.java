/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import rx.Observable;
import rx.functions.Func1;

/**
 * Performs an online query to the Parse.com database to query users.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class UsersUpdateWorker extends BaseQueryWorker<User, UsersUpdateListener> {

    private static final String WORKER_TAG = UsersUpdateWorker.class.getCanonicalName();
    @Inject
    ApiRepository mApiRepository;

    public UsersUpdateWorker() {
        // required empty constructor
    }

    /**
     * Attaches a new instance of a {@link UsersUpdateWorker}.
     *
     * @param fm the fragment manager to use for the transaction
     * @return a new instance of a {@link UsersUpdateWorker}
     */
    public static UsersUpdateWorker attach(@NonNull FragmentManager fm) {
        UsersUpdateWorker worker = (UsersUpdateWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new UsersUpdateWorker();
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
    protected Observable<User> getObservable(@NonNull Bundle args) {
        if (setCurrentGroups()) {
            return mApiRepository.calcUserBalances()
                    .toObservable()
                    .flatMap(new Func1<String, Observable<User>>() {
                        @Override
                        public Observable<User> call(String s) {
                            return mUserRepo.updateUsersAsync(mCurrentUserGroups);
                        }
                    });
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<User> observable) {
        mActivity.setUsersUpdateStream(observable, WORKER_TAG);
    }
}
