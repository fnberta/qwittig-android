/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.parse.ParseInstallation;

import javax.inject.Inject;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * Resets the device's {@link ParseInstallation} object and logs out the current user.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class LogoutWorker extends BaseWorker<User, LogoutWorkerListener> {

    private static final String WORKER_TAG = LogoutWorker.class.getCanonicalName();
    private static final String KEY_DELETE_USER = "DELETE_USER";
    @Inject
    Application mAppContext;

    public LogoutWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link LogoutWorker} that either logs the user out or deletes his
     * account.
     *
     * @param deleteUser whether to delete the account
     * @return a new instance of {@link LogoutWorker}
     */
    public static LogoutWorker attach(@NonNull FragmentManager fm, boolean deleteUser) {
        LogoutWorker worker = (LogoutWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LogoutWorker();
            final Bundle args = new Bundle();
            args.putBoolean(KEY_DELETE_USER, deleteUser);
            worker.setArguments(args);

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

    @Nullable
    @Override
    protected Observable<User> getObservable(@NonNull Bundle args) {
        final boolean delete = args.getBoolean(KEY_DELETE_USER, false);
        final User user = mUserRepo.getCurrentUser();
        if (user == null) {
            return null;
        }

        if (user.isGoogleUser()) {
            if (delete) {
                return mUserRepo.unlinkGoogle(mAppContext, user)
                        .flatMap(new Func1<User, Single<ParseInstallation>>() {
                            @Override
                            public Single<ParseInstallation> call(User user) {
                                return mUserRepo.clearInstallation();
                            }
                        })
                        .flatMap(new Func1<ParseInstallation, Single<User>>() {
                            @Override
                            public Single<User> call(ParseInstallation parseInstallation) {
                                return mUserRepo.deleteUser(user);
                            }
                        })
                        .toObservable();
            }

            return mUserRepo.signOutGoogle(mAppContext)
                    .flatMap(new Func1<Void, Single<ParseInstallation>>() {
                        @Override
                        public Single<ParseInstallation> call(Void aVoid) {
                            return mUserRepo.clearInstallation();
                        }
                    })
                    .flatMap(new Func1<ParseInstallation, Single<User>>() {
                        @Override
                        public Single<User> call(ParseInstallation parseInstallation) {
                            return mUserRepo.logOut(user);
                        }
                    })
                    .toObservable();
        }

        if (user.isFacebookUser() && delete) {
            return mUserRepo.unlinkFacebook(user)
                    .flatMap(new Func1<User, Single<ParseInstallation>>() {
                        @Override
                        public Single<ParseInstallation> call(User user) {
                            return mUserRepo.clearInstallation();
                        }
                    })
                    .flatMap(new Func1<ParseInstallation, Single<User>>() {
                        @Override
                        public Single<User> call(ParseInstallation parseInstallation) {
                            return mUserRepo.deleteUser(user);
                        }
                    })
                    .toObservable();
        }

        return mUserRepo.clearInstallation()
                .flatMap(new Func1<ParseInstallation, Single<User>>() {
                    @Override
                    public Single<User> call(ParseInstallation parseInstallation) {
                        return delete
                                ? mUserRepo.deleteUser(user)
                                : mUserRepo.logOut(user);
                    }
                })
                .toObservable();
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<User> observable) {
        mActivity.setLogoutStream(observable.toSingle(), WORKER_TAG);
    }
}
