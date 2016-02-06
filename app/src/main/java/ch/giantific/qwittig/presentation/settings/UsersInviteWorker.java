/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Invites new users to a {@link Group}.
 * <p/>
 * Subclasses {@link BaseWorker}.
 */
public class UsersInviteWorker extends BaseWorker<String, UsersInviteListener> {

    public static final String WORKER_TAG = "INVITE_USERS_WORKER";
    private static final String KEY_USERS_TO_INVITE = "USERS_TO_INVITE";
    private static final String KEY_GROUP_NAME = "GROUP_NAME";
    @Inject
    ApiRepository mApiRepo;

    public UsersInviteWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link UsersInviteWorker} with the users to invite the name of the
     * group as arguments.
     *
     * @param usersToInvite the users to invite to the group
     * @param groupName     the name of the group, used to display in the notification
     * @return a new instance of {@link UsersInviteWorker}
     */
    @NonNull
    public static UsersInviteWorker newInstance(@NonNull ArrayList<String> usersToInvite,
                                                @NonNull String groupName) {
        UsersInviteWorker fragment = new UsersInviteWorker();
        Bundle args = new Bundle();
        args.putStringArrayList(KEY_USERS_TO_INVITE, usersToInvite);
        args.putString(KEY_GROUP_NAME, groupName);
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
    protected Observable<String> getObservable(@NonNull Bundle args) {
        final List<String> usersToInvite = args.getStringArrayList(KEY_USERS_TO_INVITE);
        final String groupName = args.getString(KEY_GROUP_NAME);
        if (usersToInvite != null && !usersToInvite.isEmpty() && !TextUtils.isEmpty(groupName)) {
            return mApiRepo.inviteUsers(usersToInvite, groupName).toObservable();
        }

        return null;
    }

    @Override
    protected void setStream(@NonNull Observable<String> observable) {
        mActivity.setStatsCalcStream(observable.toSingle(), WORKER_TAG);
    }
}
