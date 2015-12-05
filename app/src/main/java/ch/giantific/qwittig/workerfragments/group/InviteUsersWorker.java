/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.workerfragments.BaseWorker;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.data.rest.CloudCodeClient;

/**
 * Invites new users to a {@link Group}.
 * <p/>
 * Subclasses {@link BaseWorker}.
 */
public class InviteUsersWorker extends BaseWorker implements
        CloudCodeClient.CloudCodeListener {

    private static final String BUNDLE_USERS_TO_INVITE = "BUNDLE_USERS_TO_INVITE";
    private static final String BUNDLE_GROUP_NAME = "BUNDLE_GROUP_NAME";
    private static final String LOG_TAG = InviteUsersWorker.class.getSimpleName();
    @Nullable
    private WorkerInteractionListener mListener;

    public InviteUsersWorker() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link InviteUsersWorker} with the users to invite the name of the
     * group as arguments.
     *
     * @param usersToInvite the users to invite to the group
     * @param groupName     the name of the group, used to display in the notification
     * @return a new instance of {@link InviteUsersWorker}
     */
    @NonNull
    public static InviteUsersWorker newInstance(@NonNull ArrayList<String> usersToInvite,
                                                @NonNull String groupName) {
        InviteUsersWorker fragment = new InviteUsersWorker();
        Bundle args = new Bundle();
        args.putStringArrayList(BUNDLE_USERS_TO_INVITE, usersToInvite);
        args.putString(BUNDLE_GROUP_NAME, groupName);
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

        List<String> usersToInvite = null;
        String groupName = "";
        Bundle args = getArguments();
        if (args != null) {
            usersToInvite = args.getStringArrayList(BUNDLE_USERS_TO_INVITE);
            groupName = args.getString(BUNDLE_GROUP_NAME);
        }

        if (usersToInvite == null || usersToInvite.isEmpty() || TextUtils.isEmpty(groupName)) {
            if (mListener != null) {
                mListener.onInviteUsersFailed(R.string.toast_unknown_error);
            }
            return;
        }

        CloudCodeClient cloudCode = new CloudCodeClient(getActivity());
        cloudCode.inviteUsers(usersToInvite, groupName, this);
    }

    @Override
    public void onCloudFunctionReturned(Object result) {
        if (mListener != null) {
            mListener.onUsersInvited();
        }
    }

    @Override
    public void onCloudFunctionFailed(@StringRes int errorMessage) {
        if (mListener != null) {
            mListener.onInviteUsersFailed(errorMessage);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the action to take after users were invited or the invitation failed.
     */
    public interface WorkerInteractionListener {
        /**
         * Handles the successful invitation of new users.
         */
        void onUsersInvited();

        /**
         * Handles the failed invitation of new users.
         *
         * @param errorMessage the error message thrown during the process
         */
        void onInviteUsersFailed(@StringRes int errorMessage);
    }
}
