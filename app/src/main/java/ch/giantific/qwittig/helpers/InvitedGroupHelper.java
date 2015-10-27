/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.receivers.PushBroadcastReceiver;

/**
 * Handles the process of a user being invited to a group and he/she accepting the invitation and
 * joining the group.
 * <p/>
 * Subclass of {@link BaseHelper}.
 */
public class InvitedGroupHelper extends BaseHelper {

    private static final String BUNDLE_GROUP_ID = "BUNDLE_GROUP_ID";
    private static final String LOG_TAG = InvitedGroupHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;
    @Nullable
    private String mGroupId;

    public InvitedGroupHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link InvitedGroupHelper} with the object id of the group the user
     * is invited to as an argument.
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
            mGroupId = args.getString(BUNDLE_GROUP_ID);
        }

        if (!TextUtils.isEmpty(mGroupId)) {
            addUserToGroupRole(mGroupId);
        }
    }

    private void addUserToGroupRole(final String groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP, groupId);
        ParseCloud.callFunctionInBackground(CloudCode.GROUP_ROLE_ADD_USER, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, @Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onInvitedGroupQueryFailed(e);
                    }
                    return;
                }

                queryGroup(groupId);
            }
        });
    }

    private void queryGroup(String groupId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Group.CLASS);
        query.getInBackground(groupId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, @Nullable ParseException e) {
                if (e != null) {
                    removeUserFromGroupRole(mGroupId);

                    if (mListener != null) {
                        mListener.onInvitedGroupQueryFailed(e);
                    }
                    return;
                }

                onGroupQueried(parseObject);
            }
        });
    }

    private void removeUserFromGroupRole(String groupId) {
        Map<String, Object> params = new HashMap<>();
        params.put(PushBroadcastReceiver.PUSH_PARAM_GROUP, groupId);
        ParseCloud.callFunctionInBackground(CloudCode.GROUP_ROLE_REMOVE_USER, params);
    }

    private void onGroupQueried(ParseObject parseObject) {
        Group group = (Group) parseObject;
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser != null && group.getUsersInvited().contains(currentUser.getUsername())) {
            if (mListener != null) {
                mListener.onInvitedGroupQueried(parseObject);
            }
        } else {
            removeUserFromGroupRole(mGroupId);

            if (mListener != null) {
                mListener.onEmailNotValid();
            }
        }
    }

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
                        mListener.onUserJoinGroupFailed(e);
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
         * @param e the {@link ParseException} thrown during the process
         */
        void onInvitedGroupQueryFailed(@NonNull ParseException e);

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
         * @param e the {@link ParseException} thrown during the process
         */
        void onUserJoinGroupFailed(@NonNull ParseException e);
    }
}
