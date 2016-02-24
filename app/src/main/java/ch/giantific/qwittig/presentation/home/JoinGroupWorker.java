/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import javax.inject.Inject;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

/**
 * Handles the process of a user being invited to a group and he/she accepting the invitation and
 * joining the group.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class JoinGroupWorker extends BaseWorker<User, JoinGroupWorkerListener> {

    private static final String WORKER_TAG = JoinGroupWorker.class.getCanonicalName();
    private static final String KEY_IDENTITY_ID = "IDENTITY_ID";
    @Inject
    IdentityRepository mIdentityRepo;
    @Inject
    GroupRepository mGroupRepo;

    public JoinGroupWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link JoinGroupWorker} with the object id of the group the
     * user is invited to as an argument.
     *
     * @param fm         the fragment manager to use for the transaction
     * @param identityId the object id of the identity the user is invited to
     * @return a new instance of {@link JoinGroupWorker}
     */
    @NonNull
    public static JoinGroupWorker attach(@NonNull FragmentManager fm, @NonNull String identityId) {
        JoinGroupWorker worker = (JoinGroupWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new JoinGroupWorker();
            final Bundle args = new Bundle();
            args.putString(KEY_IDENTITY_ID, identityId);
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
        final String identityId = args.getString(KEY_IDENTITY_ID);
        final User currentUser = mUserRepo.getCurrentUser();
        if (currentUser != null && !TextUtils.isEmpty(identityId)) {
            return mUserRepo.handleInvitation(identityId)
                    .flatMap(new Func1<String, Single<User>>() {
                        @Override
                        public Single<User> call(String s) {
                            return mUserRepo.updateUser(currentUser);
                        }
                    })
                    .flatMap(new Func1<User, Single<Identity>>() {
                        @Override
                        public Single<Identity> call(User user) {
                            return mIdentityRepo.fetchIdentityDataAsync(user.getCurrentIdentity());
                        }
                    })
                    .flatMap(new Func1<Identity, Single<Identity>>() {
                        @Override
                        public Single<Identity> call(Identity identity) {
                            return mIdentityRepo.saveIdentityLocalAsync(identity);
                        }
                    })
                    .flatMap(new Func1<Identity, Single<Group>>() {
                        @Override
                        public Single<Group> call(Identity identity) {
                            return mGroupRepo.subscribeGroup(identity.getGroup());
                        }
                    })
                    .map(new Func1<Group, User>() {
                        @Override
                        public User call(Group group) {
                            return currentUser;
                        }
                    })
                    .toObservable();
        }

        return null;
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<User> observable) {
        mActivity.setJoinGroupStream(observable.toSingle(), WORKER_TAG);
    }
}
