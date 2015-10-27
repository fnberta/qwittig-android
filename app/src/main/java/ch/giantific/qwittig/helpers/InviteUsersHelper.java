/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.data.parse.models.Group;

/**
 * Invites new users to a {@link Group}.
 * <p/>
 * Subclasses {@link BaseInviteHelper}.
 */
public class InviteUsersHelper extends BaseInviteHelper {

    private static final String BUNDLE_USERS_TO_INVITE = "BUNDLE_USERS_TO_INVITE";
    private static final String BUNDLE_GROUP_NAME = "BUNDLE_GROUP_NAME";
    private static final String LOG_TAG = InviteUsersHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;

    public InviteUsersHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link InviteUsersHelper} with the users to invite the name of the
     * group as arguments.
     *
     * @param usersToInvite the users to invite to the group
     * @param groupName     the name of the group, used to display in the notification
     * @return a new instance of {@link InviteUsersHelper}
     */
    @NonNull
    public static InviteUsersHelper newInstance(@NonNull ArrayList<String> usersToInvite,
                                                @NonNull String groupName) {
        InviteUsersHelper fragment = new InviteUsersHelper();
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
            mListener = (HelperInteractionListener) activity;
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
            return;
        }

        inviteUsers(usersToInvite, groupName);
    }

    @Override
    protected void onInviteUsersFailed(ParseException e) {
        if (mListener != null) {
            mListener.onInviteUsersFailed(e);
        }
    }

    @Override
    protected void onUsersInvited() {
        if (mListener != null) {
            mListener.onUsersInvited();
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
    public interface HelperInteractionListener {
        /**
         * Handles the successful invitation of new users.
         */
        void onUsersInvited();

        /**
         * Handles the failed invitation of new users.
         *
         * @param e the {@link ParseException} thrown during the process
         */
        void onInviteUsersFailed(@NonNull ParseException e);
    }
}
