/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup;

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
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Creates a new {@link Group}, saves it to the online Parse.com database and invites the users
 * specified.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class AddGroupWorker extends BaseWorker<Identity, AddGroupWorkerListener> {

    private static final String WORKER_TAG = AddGroupWorker.class.getCanonicalName();
    private static final String KEY_GROUP_NAME = "GROUP_NAME";
    private static final String KEY_GROUP_CURRENCY = "GROUP_CURRENCY";
    @Inject
    GroupRepository mGroupRepo;

    public AddGroupWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link AddGroupWorker} with the group name and currency.
     *
     * @param fm            the fragment manager to user for the transaction
     * @param groupName     the name of the new group to create
     * @param groupCurrency the currency of the new group to create
     * @return a new instance of {@link AddGroupWorker}
     */
    public static AddGroupWorker attach(@NonNull FragmentManager fm, @NonNull String groupName,
                                        @NonNull String groupCurrency) {
        AddGroupWorker worker = (AddGroupWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new AddGroupWorker();
            final Bundle args = new Bundle();
            args.putString(KEY_GROUP_NAME, groupName);
            args.putString(KEY_GROUP_CURRENCY, groupCurrency);
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
        final String groupName = args.getString(KEY_GROUP_NAME);
        final String groupCurrency = args.getString(KEY_GROUP_CURRENCY);
        final User user = mUserRepo.getCurrentUser();
        if (user != null && !TextUtils.isEmpty(groupName) && !TextUtils.isEmpty(groupCurrency)) {
            return mUserRepo.addNewGroup(user, groupName, groupCurrency).toObservable();
        }

        return null;
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<Identity> observable) {
        mActivity.setCreateGroupStream(observable.toSingle(), WORKER_TAG);
    }
}
