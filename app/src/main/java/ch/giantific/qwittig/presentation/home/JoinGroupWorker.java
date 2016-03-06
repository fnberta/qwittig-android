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

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Handles the process of a user being invited to a group and he/she accepting the invitation and
 * joining the group.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class JoinGroupWorker extends BaseWorker<Identity, JoinGroupWorkerListener> {

    private static final String WORKER_TAG = JoinGroupWorker.class.getCanonicalName();
    private static final String KEY_IDENTITY_ID = "IDENTITY_ID";
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
    protected Observable<Identity> getObservable(@NonNull Bundle args) {
        final String identityId = args.getString(KEY_IDENTITY_ID);
        final User currentUser = mUserRepo.getCurrentUser();
        if (currentUser != null && !TextUtils.isEmpty(identityId)) {
            return mUserRepo.handleInvitation(currentUser, identityId).toObservable();
        }

        return null;
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<Identity> observable) {
        mActivity.setJoinGroupStream(observable.toSingle(), WORKER_TAG);
    }
}
