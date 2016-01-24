/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import rx.Observable;
import rx.functions.Func1;

/**
 * Performs an online query to the Parse.com database to query users.
 * <p/>
 * Subclass of {@link BaseQueryWorker}.
 */
public class UsersUpdateWorker extends BaseQueryWorker<User, UsersUpdateListener> {

    public static final String WORKER_TAG = "USER_UPDATE_WORKER";
    private static final String LOG_TAG = UsersUpdateWorker.class.getSimpleName();
    @Inject
    UserRepository mUserRepo;
    @Inject
    ApiRepository mApiRepository;

    @Override
    protected String getWorkerTag() {
        return WORKER_TAG;
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
    protected void setStream(@NonNull Observable<User> observable, @NonNull String workerTag) {
        mActivity.setUsersUpdateStream(observable, workerTag);
    }
}
