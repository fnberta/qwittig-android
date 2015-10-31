/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import ch.giantific.qwittig.data.helpers.BaseHelper;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.repositories.ParseGroupRepository;
import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.domain.repositories.GroupRepository;

/**
 * Handles the process of a user being invited to a group and he/she accepting the invitation and
 * joining the group.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class InvitedGroupHelper extends BaseHelper implements
        CloudCodeClient.CloudCodeListener,
        GroupRepository.GetGroupOnlineListener {

    private static final String BUNDLE_GROUP_ID = "BUNDLE_GROUP_ID";
    private static final String LOG_TAG = InvitedGroupHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;
    private String mGroupId;
    private CloudCodeClient mCloudCode;

    public InvitedGroupHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link InvitedGroupHelper} with the object id of the group the
     * user is invited to as an argument.
     *
     * @param groupId the object id of the group the user is invited to
     * @return a new instance of {@link InvitedGroupHelper}
     */
    @NonNull
    public static InvitedGroupHelper newInstance(String groupId) {
        InvitedGroupHelper fragment = new InvitedGroupHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
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
                mListener.onInvitedGroupQueryFailed(0);
            }

            return;
        }

        mCloudCode = new CloudCodeClient();
        mCloudCode.addUserToGroupRole(mGroupId, this);
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        queryGroup();
    }

    @Override
    public void onCloudFunctionFailed(int errorCode) {
        if (mListener != null) {
            mListener.onInvitedGroupQueryFailed(errorCode);
        }
    }

    private void queryGroup() {
        GroupRepository repo = new ParseGroupRepository();
        repo.getGroupOnlineAsync(mGroupId, this);
    }

    @Override
    public void onGroupOnlineLoaded(@NonNull Group group) {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser != null && group.getUsersInvited().contains(currentUser.getUsername())) {
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
    public void onGroupOnlineLoadFailed(int errorCode) {
        mCloudCode.removeUserFromGroupRole(mGroupId);
        if (mListener != null) {
            mListener.onInvitedGroupQueryFailed(errorCode);
        }
    }

    /**
     * Adds the group to the current user's groups and sets it as his/her current group. Save the
     * user object.
     *
     * @param invitedGroup the group the user wants to join
     */
    public void joinInvitedGroup(final ParseObject invitedGroup) {
        final User currentUser = (User) ParseUser.getCurrentUser();
        final Group currentGroup = currentUser.getCurrentGroup();
        // user needs to be saved before group, otherwise check in CloudCode will fail and user
        // will be removed from group Role!
        currentUser.addGroup(invitedGroup);
        currentUser.setCurrentGroup(invitedGroup);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    currentUser.removeGroup(invitedGroup);
                    currentUser.setCurrentGroup(currentGroup);

                    if (mListener != null) {
                        mListener.onUserJoinGroupFailed(e.getCode());
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
    public interface HelperInteractionListener {
        /**
         * Handles the successful query of the group the user is invited to
         *
         * @param group the queried group
         */
        void onInvitedGroupQueried(@NonNull ParseObject group);

        /**
         * Handles the failure to query of the group the user is invited to.
         *
         * @param errorCode the error code of the exception thrown during the process
         */
        void onInvitedGroupQueryFailed(int errorCode);

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
         * @param errorCode the error code of the exception thrown during the process
         */
        void onUserJoinGroupFailed(int errorCode);
    }
}
