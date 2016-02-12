/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.parse.ParseObject;

import javax.inject.Inject;

import ch.giantific.qwittig.di.components.WorkerComponent;
import ch.giantific.qwittig.domain.models.Group;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.domain.repositories.GroupRepository;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Handles the process of a user being invited to a group and he/she accepting the invitation and
 * joining the group.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class InvitedGroupWorker extends BaseWorker<Group, InvitedGroupWorkerListener> {

    private static final String KEY_GROUP_ID = "GROUP_ID";
    @Inject
    GroupRepository mGroupRepo;
    private String mGroupId;
    private User mCurrentUser;

    public InvitedGroupWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link InvitedGroupWorker} with the object id of the group the
     * user is invited to as an argument.
     *
     * @param groupId the object id of the group the user is invited to
     * @return a new instance of {@link InvitedGroupWorker}
     */
    @NonNull
    public static InvitedGroupWorker newInstance(String groupId) {
        InvitedGroupWorker fragment = new InvitedGroupWorker();
        Bundle args = new Bundle();
        args.putString(KEY_GROUP_ID, groupId);
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
//        mGroupId = args.getString(KEY_GROUP_ID, "");
//        mCurrentUser = mUserRepo.getCurrentUser();
//        if (!TextUtils.isEmpty(mGroupId) && mCurrentUser != null) {
//            return mCloudCode.addUserToGroupRole(mGroupId)
//                    .flatMap(new Func1<String, Single<Group>>() {
//                        @Override
//                        public Single<Group> call(String s) {
//                            return mGroupRepo.getGroupOnlineAsync(mGroupId);
//                        }
//                    })
//                    .toObservable();
//        }

        return null;
    }

    @Override
    protected void onError() {

    }

    @Override
    protected void setStream(@NonNull Observable<Group> observable) {

    }

    public void onGroupOnlineLoaded(@NonNull Group group) {
//        if (group.getUsersInvited().contains(mCurrentUser.getUsername())) {
//            if (mListener != null) {
//                mListener.onInvitedGroupQueried(group);
//            }
//        } else {
//            mCloudCode.removeUserFromGroupRole(mGroupId);
//
//            if (mListener != null) {
//                mListener.onEmailNotValid();
//            }
//        }
    }

    public void onGroupOnlineLoadFailed(@StringRes int errorMessage) {
//        mCloudCode.removeUserFromGroupRole(mGroupId);
//        if (mListener != null) {
//            mListener.onInvitedGroupQueryFailed(errorMessage);
//        }
    }

    /**
     * Adds the group to the current user's groups and sets it as his/her current group. Save the
     * user object.
     *
     * @param invitedGroup the group the user wants to join
     */
    public void joinInvitedGroup(final ParseObject invitedGroup) {
//        final Group currentGroup = mCurrentUser.getCurrentGroup();
//        // user needs to be saved before group, otherwise check in CloudCode will fail and user
//        // will be removed from group Role!
//        mCurrentUser.addGroup(invitedGroup);
//        mCurrentUser.setCurrentGroup(invitedGroup);
//        mCurrentUser.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(@Nullable ParseException e) {
//                if (e != null) {
//                    mCurrentUser.removeGroup(invitedGroup);
//                    mCurrentUser.setCurrentGroup(currentGroup);
//
//                    if (mListener != null) {
//                        mListener.onUserJoinGroupFailed(ParseErrorHandler.handleParseError(getActivity(), e));
//                    }
//                }
//
//                if (mListener != null) {
//                    mListener.onUserJoinedGroup();
//                }
//            }
//        });
    }

}
