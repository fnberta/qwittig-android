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
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Creates a new {@link Group}, saves it to the online Parse.com database and invites the users
 * specified.
 * <p/>
 * Subclass of {@link BaseInviteHelper}.
 */
public class CreateGroupHelper extends BaseInviteHelper {

    private static final String BUNDLE_GROUP_NAME = "BUNDLE_GROUP_NAME";
    private static final String BUNDLE_GROUP_CURRENCY = "BUNDLE_GROUP_CURRENCY";
    private static final String BUNDLE_USERS_TO_INVITE = "BUNDLE_USERS_TO_INVITE";
    private static final String LOG_TAG = CreateGroupHelper.class.getSimpleName();
    @Nullable
    private HelperInteractionListener mListener;
    @Nullable
    private List<String> mUsersToInvite;

    public CreateGroupHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link CreateGroupHelper} with the group name, currency and the
     * users to invite as arguments.
     *
     * @param groupName     the name of the new group to create
     * @param groupCurrency the currency of the new group to create
     * @param usersToInvite the users to invite into the newly crated group
     * @return a new instance of {@link CreateGroupHelper}
     */
    @NonNull
    public static CreateGroupHelper newInstance(@NonNull String groupName,
                                                @NonNull String groupCurrency,
                                                @NonNull ArrayList<String> usersToInvite) {
        CreateGroupHelper fragment = new CreateGroupHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_GROUP_NAME, groupName);
        args.putString(BUNDLE_GROUP_CURRENCY, groupCurrency);
        args.putStringArrayList(BUNDLE_USERS_TO_INVITE, usersToInvite);
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

        String groupName = "";
        String groupCurrency = "";

        Bundle args = getArguments();
        if (args != null) {
            groupName = args.getString(BUNDLE_GROUP_NAME);
            groupCurrency = args.getString(BUNDLE_GROUP_CURRENCY);
            mUsersToInvite = args.getStringArrayList(BUNDLE_USERS_TO_INVITE);
        }

        if (TextUtils.isEmpty(groupName) || TextUtils.isEmpty(groupCurrency)) {
            // TODO: callback to caller that helper should be removed
            return;
        }

        createNewGroup(groupName, groupCurrency);
    }

    private void createNewGroup(@NonNull final String groupName, @NonNull String groupCurrency) {
        final User currentUser = (User) ParseUser.getCurrentUser();
        final Group groupOld = currentUser.getCurrentGroup();
        final Group groupNew = new Group(groupName, groupCurrency);
        currentUser.addGroup(groupNew);
        currentUser.setCurrentGroup(groupNew);
        // We use saveInBackground because we need the object to have an objectId when
        // SettingsFragment starts
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onCreateNewGroupFailed(e);
                    }

                    currentUser.removeGroup(groupNew);
                    currentUser.setCurrentGroup(groupOld);
                    return;
                }

                boolean invitingUsers = mUsersToInvite != null && !mUsersToInvite.isEmpty();
                if (mListener != null) {
                    mListener.onNewGroupCreated(groupNew, invitingUsers);
                }

                if (invitingUsers) {
                    // If user has listed people to invite, invite them
                    inviteUsers(mUsersToInvite, groupName);
                }
            }
        });
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
     * Defines the actions to take after a new group was successfully created or when the creation
     * failed.
     */
    public interface HelperInteractionListener {
        /**
         * Handles the successul creation of the new group.
         *
         * @param newGroup     the newly created {@link Group} object
         * @param invitingUser whether there is process ongoing to invite new users
         */
        void onNewGroupCreated(@NonNull Group newGroup, boolean invitingUser);

        /**
         * Handles the failure of the creation of the new group.
         *
         * @param e the {@link ParseException} thrown during the process
         */
        void onCreateNewGroupFailed(@NonNull ParseException e);

        /**
         * Handles the successful invitation of the users to the new group.
         */
        void onUsersInvited();

        /**
         * Handles the case when the invitation of the users failed.
         *
         * @param e the {@link ParseException} thrown during the process
         */
        void onInviteUsersFailed(@NonNull ParseException e);
    }
}
