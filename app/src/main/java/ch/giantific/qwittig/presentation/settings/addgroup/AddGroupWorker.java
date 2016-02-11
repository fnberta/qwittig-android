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

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.domain.repositories.IdentityRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

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
    @Inject
    IdentityRepository mIdentityRepo;

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
            worker = newInstance(groupName, groupCurrency);
            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    @NonNull
    private static AddGroupWorker newInstance(@NonNull String groupName,
                                              @NonNull String groupCurrency) {
        AddGroupWorker fragment = new AddGroupWorker();
        Bundle args = new Bundle();
        args.putString(KEY_GROUP_NAME, groupName);
        args.putString(KEY_GROUP_CURRENCY, groupCurrency);
        fragment.setArguments(args);
        return fragment;
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
        if (!TextUtils.isEmpty(groupName) && !TextUtils.isEmpty(groupCurrency)) {
            return mGroupRepo.addNewGroup(groupName, groupCurrency)
                    .flatMap(new Func1<String, Single<User>>() {
                        @Override
                        public Single<User> call(String s) {
                            return mUserRepo.updateUser(mUserRepo.getCurrentUser());
                        }
                    })
                    .flatMap(new Func1<User, Single<Identity>>() {
                        @Override
                        public Single<Identity> call(User user) {
                            final Identity identity = user.getCurrentIdentity();
                            return mIdentityRepo.saveIdentityLocalAsync(identity);
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
    protected void setStream(@NonNull Observable<Identity> observable) {
        mActivity.setCreateGroupStream(observable.toSingle(), WORKER_TAG);
    }
}
