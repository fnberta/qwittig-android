/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.group;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.workerfragments.BaseWorker;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.domain.repositories.GroupRepository;

/**
 * Handles the process of a user being invited to a group and he/she accepting the invitation and
 * joining the group.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class InvitedGroupWorker extends BaseWorker implements
        CloudCodeClient.CloudCodeListener,
        GroupRepository.GetGroupOnlineListener {

    private static final String BUNDLE_GROUP_ID = "BUNDLE_GROUP_ID";
    private static final String LOG_TAG = InvitedGroupWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;
    private String mGroupId;
    private CloudCodeClient mCloudCode;
    private User mCurrentUser;
    private GroupRepository mGroupRepo;

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
        args.putString(BUNDLE_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WorkerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mGroupId = args.getString(BUNDLE_GROUP_ID, "");
        }

        if (TextUtils.isEmpty(mGroupId)) {
            if (mListener != null) {
                mListener.onInvitedGroupQueryFailed(R.string.toast_unknown_error);
            }

            return;
        }

        mCurrentUser = (User) ParseUser.getCurrentUser();
        final Context context = getActivity();
        mGroupRepo = new ParseGroupRepository(context);
        mCloudCode = new CloudCodeClient(context);
        mCloudCode.addUserToGroupRole(mGroupId, this);
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        queryGroup();
    }

    @Override
    public void onCloudFunctionFailed(@StringRes int errorMessage) {
        if (mListener != null) {
            mListener.onInvitedGroupQueryFailed(errorMessage);
        }
    }

    private void queryGroup() {
        mGroupRepo.getGroupOnlineAsync(mGroupId, this);
    }

    @Override
    public void onGroupOnlineLoaded(@NonNull Group group) {
        if (group.getUsersInvited().contains(mCurrentUser.getUsername())) {
            if (mListener != null) {
                mListener.onInvitedGroupQueried(group);
            }
        } else {
            mCloudCode.removeUserFromGroupRole(mGroupId);

            if (mListener != null) {
                mListener.onEmailNotValid();
            }
        }
    }

    @Override
    public void onGroupOnlineLoadFailed(@StringRes int errorMessage) {
        mCloudCode.removeUserFromGroupRole(mGroupId);
        if (mListener != null) {
            mListener.onInvitedGroupQueryFailed(errorMessage);
        }
    }

    /**
     * Adds the group to the current user's groups and sets it as his/her current group. Save the
     * user object.
     *
     * @param invitedGroup the group the user wants to join
     */
    public void joinInvitedGroup(final ParseObject invitedGroup) {
        final Group currentGroup = mCurrentUser.getCurrentGroup();
        // user needs to be saved before group, otherwise check in CloudCode will fail and user
        // will be removed from group Role!
        mCurrentUser.addGroup(invitedGroup);
        mCurrentUser.setCurrentGroup(invitedGroup);
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    mCurrentUser.removeGroup(invitedGroup);
                    mCurrentUser.setCurrentGroup(currentGroup);

                    if (mListener != null) {
                        mListener.onUserJoinGroupFailed(ParseErrorHandler.handleParseError(getActivity(), e));
                    }
                }

                if (mListener != null) {
                    mListener.onUserJoinedGroup();
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines actions to be taken during the invited group querying and joining process.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful query of the group the user is invited to
         *
         * @param group the queried group
         */
        void onInvitedGroupQueried(@NonNull ParseObject group);

        /**
         * Handles the failure to query of the group the user is invited to.
         *
         * @param errorMessage the error message from the exception thrown during the process
         */
        void onInvitedGroupQueryFailed(@StringRes int errorMessage);

        /**
         * Handles the case when user's email was removed from the users invited to get group
         */
        void onEmailNotValid();

        /**
         * Handles the case when the user successfully joined the new group
         */
        void onUserJoinedGroup();

        /**
         * Handles the failure to join the new group.
         *
         * @param errorMessage the error message from the exception thrown during the process
         */
        void onUserJoinGroupFailed(@StringRes int errorMessage);
    }
}
