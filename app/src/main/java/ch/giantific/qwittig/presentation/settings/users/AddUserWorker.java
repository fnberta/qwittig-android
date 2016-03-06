/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Invites new users to a {@link Group}.
 * <p/>
 * Subclasses {@link BaseWorker}.
 */
public class AddUserWorker extends BaseWorker<Identity, AddUserWorkerListener> {

    private static final String WORKER_TAG = AddUserWorker.class.getCanonicalName();
    private static final String KEY_NICKNAME = "NICKNAME";
    private static final String KEY_GROUP_ID = "GROUP_ID";
    private static final String KEY_GROUP_NAME = "GROUP_NAME";

    public AddUserWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link AddUserWorker} with the users to invite the name of the
     * group as arguments.
     *
     * @param fm        the fragment manager to use for the transaction.
     * @param nickname  the nickname to add an identity for
     * @param groupName the name of the group, used to display in the invite link
     * @return a new instance of {@link AddUserWorker}
     */
    public static AddUserWorker attach(@NonNull FragmentManager fm,
                                       @NonNull String nickname,
                                       @NonNull String groupId,
                                       @NonNull String groupName) {
        AddUserWorker worker = (AddUserWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new AddUserWorker();
            final Bundle args = new Bundle();
            args.putString(KEY_NICKNAME, nickname);
            args.putString(KEY_GROUP_ID, groupId);
            args.putString(KEY_GROUP_NAME, groupName);
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

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Nullable
    @Override
    protected Observable<Identity> getObservable(@NonNull Bundle args) {
        final String nickname = args.getString(KEY_NICKNAME);
        final String groupId = args.getString(KEY_GROUP_ID);
        final String groupName = args.getString(KEY_GROUP_NAME);
        if (!TextUtils.isEmpty(nickname) && !TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(groupName)) {
            return mUserRepo.addIdentity(nickname, groupId, groupName).toObservable();
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<Identity> observable) {
        mActivity.setAddUserStream(observable.toSingle(), WORKER_TAG);
    }
}
