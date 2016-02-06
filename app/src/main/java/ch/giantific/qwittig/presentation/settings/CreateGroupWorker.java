/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.repositories.ApiRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Creates a new {@link Group}, saves it to the online Parse.com database and invites the users
 * specified.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class CreateGroupWorker extends BaseWorker<Group, CreateGroupWorkerListener> {

    public static final String WORKER_TAG = "CREATE_GROUP_WORKER";
    private static final String KEY_GROUP_NAME = "GROUP_NAME";
    private static final String KEY_GROUP_CURRENCY = "GROUP_CURRENCY";
    private static final String KEY_USERS_TO_INVITE = "USERS_TO_INVITE";
    @Inject
    ApiRepository mApiRepo;
    @Nullable
    private List<String> mUsersToInvite;

    public CreateGroupWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link CreateGroupWorker} with the group name, currency and the
     * users to invite as arguments.
     *
     * @param groupName     the name of the new group to create
     * @param groupCurrency the currency of the new group to create
     * @param usersToInvite the users to invite into the newly crated group
     * @return a new instance of {@link CreateGroupWorker}
     */
    @NonNull
    public static CreateGroupWorker newInstance(@NonNull String groupName,
                                                @NonNull String groupCurrency,
                                                @NonNull ArrayList<String> usersToInvite) {
        CreateGroupWorker fragment = new CreateGroupWorker();
        Bundle args = new Bundle();
        args.putString(KEY_GROUP_NAME, groupName);
        args.putString(KEY_GROUP_CURRENCY, groupCurrency);
        args.putStringArrayList(KEY_USERS_TO_INVITE, usersToInvite);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @Nullable
    @Override
    protected Observable<Group> getObservable(@NonNull Bundle args) {
        return null;
    }

    @Override
    protected void onError() {

    }

    @Override
    protected void setStream(@NonNull Observable<Group> observable) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//
//        String groupName = "";
//        String groupCurrency = "";
//
//        Bundle args = getArguments();
//        if (args != null) {
//            groupName = args.getString(KEY_GROUP_NAME);
//            groupCurrency = args.getString(KEY_GROUP_CURRENCY);
//            mUsersToInvite = args.getStringArrayList(KEY_USERS_TO_INVITE);
//        }
//
//        if (TextUtils.isEmpty(groupName) || TextUtils.isEmpty(groupCurrency)) {
//            if (mListener != null) {
//                mListener.onCreateNewGroupFailed(R.string.toast_unknown_error);
//            }
//            return;
//        }
//
//        createNewGroup(groupName, groupCurrency);
    }

    private void createNewGroup(@NonNull final String groupName, @NonNull String groupCurrency) {
//        final User currentUser = (User) ParseUser.getCurrentUser();
//        final Group groupOld = currentUser.getCurrentGroup();
//        final Group groupNew = new Group(groupName, groupCurrency);
//        currentUser.addGroup(groupNew);
//        currentUser.setCurrentGroup(groupNew);
//        // We use saveInBackground because we need the object to have an objectId when
//        // SettingsFragment starts
//        currentUser.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(@Nullable ParseException e) {
//                if (e != null) {
//                    if (mListener != null) {
//                        mListener.onCreateNewGroupFailed(ParseErrorHandler.handleParseError(getActivity(), e));
//                    }
//
//                    currentUser.removeGroup(groupNew);
//                    currentUser.setCurrentGroup(groupOld);
//                    return;
//                }
//
//                boolean invitingUsers = mUsersToInvite != null && !mUsersToInvite.isEmpty();
//                if (mListener != null) {
//                    mListener.onNewGroupCreated(groupNew, invitingUsers);
//                }
//
//                // If user has listed people to invite, invite them
//                if (invitingUsers) {
//                    inviteUsers(groupName);
//                }
//            }
//        });
    }

    private void inviteUsers(String groupName) {
//        mApiRepo.inviteUsers(mUsersToInvite, groupName, this);
    }

    public void onCloudFunctionReturned(Object result) {
//        if (mListener != null) {
//            mListener.onUsersInvited();
//        }
    }

    public void onCloudFunctionFailed(@StringRes int errorMessage) {
//        if (mListener != null) {
//            mListener.onInviteUsersFailed(errorMessage);
//        }
    }

}
