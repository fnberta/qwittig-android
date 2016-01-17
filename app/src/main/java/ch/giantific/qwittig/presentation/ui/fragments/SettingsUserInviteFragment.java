/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.presentation.workerfragments.group.UsersInviteWorker;

/**
 * Displays the user invite screen, where the user can invite new users to the group and sees
 * everybody that is currently invited but has not yet accepted/declined the invitation.
 * <p/>
 * Subclass of {@link SettingsBaseInviteFragment}.
 */
public class SettingsUserInviteFragment extends SettingsBaseInviteFragment implements
        UserRepository.GetUsersLocalListener {

    private static final String INVITE_WORKER = "INVITE_WORKER";
    private static final String LOG_TAG = SettingsUserInviteFragment.class.getSimpleName();
    private FragmentInteractionListener mListener;
    private LinearLayout mLinearLayoutUsersInvited;
    private TextView mTextViewNoInvitations;
    @NonNull
    private List<String> mUsersInvitedEmails = new ArrayList<>();
    @NonNull
    private List<View> mUsersInvitedRows = new ArrayList<>();
    private boolean mIsInviting;

    public SettingsUserInviteFragment() {
        // Required empty public constructor
    }

    public boolean isInviting() {
        return mIsInviting;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_user_invite, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLinearLayoutUsersInvited = (LinearLayout) view.findViewById(R.id.ll_users_invited);
        mTextViewNoInvitations = (TextView) view.findViewById(R.id.tv_no_open_invitations);

        updateCurrentUserAndGroup();
        setupInvitedUsers();
        setupUsersToInviteRows();
    }

    private void setupInvitedUsers() {
        List<String> usersInvited = mCurrentGroup.getUsersInvited();

        if (!usersInvited.isEmpty()) {
            setNoInvitationsViewVisibility(false);

            for (int i = 0, usersInvitedSize = usersInvited.size(); i < usersInvitedSize; i++) {
                String email = usersInvited.get(i);
                addInvitedRow(i, email);
            }
        } else {
            setNoInvitationsViewVisibility(true);
        }
    }

    private void addInvitedRow(int position, String email) {
        mUsersInvitedEmails.add(email);

        final View invitedUserRow = getActivity().getLayoutInflater().inflate(
                R.layout.row_settings_users_invited, mLinearLayoutUsersInvited, false);
        TextView tvName = (TextView) invitedUserRow.findViewById(R.id.tv_invited_user);
        tvName.setText(email);
        View clearInvite = invitedUserRow.findViewById(R.id.iv_clear_invite);
        clearInvite.setTag(position);
        clearInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                int position = Utils.getViewPositionFromTag(v);
                removeUserInvited(position);
                updateViewTags();
            }
        });

        mUsersInvitedRows.add(invitedUserRow);
        mLinearLayoutUsersInvited.addView(invitedUserRow);
    }

    private void setNoInvitationsViewVisibility(boolean showView) {
        if (showView) {
            mTextViewNoInvitations.setVisibility(View.VISIBLE);
        } else {
            mTextViewNoInvitations.setVisibility(View.GONE);
        }
    }

    private void updateViewTags() {
        for (int i = 0, mUsersInvitedRowsSize = mUsersInvitedRows.size(); i < mUsersInvitedRowsSize; i++) {
            View invitedRow = mUsersInvitedRows.get(i);
            View clearInvite = invitedRow.findViewById(R.id.iv_clear_invite);
            clearInvite.setTag(i);
        }
    }

    private void removeUserInvited(int position) {
        mLinearLayoutUsersInvited.removeView(mUsersInvitedRows.get(position));
        mUsersInvitedRows.remove(position);
        mUsersInvitedEmails.remove(position);
        setNoInvitationsViewVisibility(mUsersInvitedEmails.isEmpty());

        mCurrentGroup.setUsersInvited(mUsersInvitedEmails);
        mCurrentGroup.saveEventually();
    }

    /**
     * Starts the invitation process checking if there is already a process going on and if not
     * checking that the entered emails are all not already invited and the queries all users of
     * the group to check if the invitees are not already in the group.
     */
    public void startInvitation() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (mIsInviting || !invitedUsersEmailsAreValid()) {
            return;
        }

        if (!allEmailsAreNotAlreadyInvited()) {
            return;
        }

        if (!Utils.isNetworkAvailable(getActivity())) {
            Snackbar.make(mTextViewNoInvitations, R.string.toast_no_connection,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        UserRepository repo = new ParseUserRepository(getActivity());
        repo.getUsersLocalAsync(mCurrentGroup);
    }

    private boolean allEmailsAreNotAlreadyInvited() {
        boolean emailsAreNotAlreadyInvited = true;
        for (int i = 0, mUsersToInviteEmailsSize = mUsersToInviteEmails.size(); i < mUsersToInviteEmailsSize; i++) {
            String email = mUsersToInviteEmails.get(i);

            TextInputLayout tilUser = mUsersToInviteFields.get(i);
            if (mCurrentGroup.getUsersInvited().contains(email)) {
                tilUser.setError(getString(R.string.toast_already_invited, email));
                emailsAreNotAlreadyInvited = false;
            } else {
                tilUser.setErrorEnabled(false);
            }
        }

        return emailsAreNotAlreadyInvited;
    }

    @Override
    public void onUsersLocalLoaded(@NonNull List<ParseUser> users) {
        if (allEmailsAreNotAlreadyInGroup(users)) {
            inviteNewUsers();
        }
    }

    private boolean allEmailsAreNotAlreadyInGroup(@NonNull List<ParseUser> users) {
        boolean allEmailsAreNotAlreadyInGroup = true;

        for (ParseUser parseUser : users) {
            User user = (User) parseUser;

            for (int i = 0, mUsersToInviteEmailsSize = mUsersToInviteEmails.size(); i < mUsersToInviteEmailsSize; i++) {
                String email = mUsersToInviteEmails.get(i);

                TextInputLayout tilUser = mUsersToInviteFields.get(i);
                if (email.equals(user.getUsername())) {
                    tilUser.setError(getString(R.string.toast_already_in_group, email));
                    allEmailsAreNotAlreadyInGroup = false;
                } else {
                    tilUser.setErrorEnabled(false);
                }
            }
        }

        return allEmailsAreNotAlreadyInGroup;
    }

    private void inviteNewUsers() {
        mIsInviting = true;
        mListener.startProgressAnim();

        inviteUsersWithWorker(mCurrentGroup);
    }

    private void inviteUsersWithWorker(@NonNull Group group) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment inviteUsersWorker = WorkerUtils.findWorker(fragmentManager, INVITE_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (inviteUsersWorker == null) {
            inviteUsersWorker = UsersInviteWorker.newInstance(mUsersToInviteEmails, group.getName());

            fragmentManager.beginTransaction()
                    .add(inviteUsersWorker, INVITE_WORKER)
                    .commit();
        }
    }

    /**
     * Shows the user the error message and removes the retained worker fragment and loading
     * indicators.
     *
     * @param errorMessage the error message thrown in the process
     */
    public void onInviteUsersFailed(@StringRes int errorMessage) {
        onInviteError(errorMessage, INVITE_WORKER);
    }

    /**
     * Starts the {@link FabProgress} final animation and removes the retained worker fragment
     * fragment.
     */
    public void onUsersInvited() {
        mListener.startFinalProgressAnim();
        WorkerUtils.removeWorker(getFragmentManager(), INVITE_WORKER);

    }

    /**
     * Finishes the invitation process by hiding the no invitations text view, adding indications
     * for every user indicated and showing the user a message that users were invited.
     */
    public void finishInvitations() {
        setNoInvitationsViewVisibility(false);
        for (String email : mUsersToInviteEmails) {
            addInvitedRow(mUsersInvitedRows.size(), email);
        }
        Snackbar.make(mTextViewNoInvitations, R.string.toast_user_invited,
                Snackbar.LENGTH_LONG).show();

        mIsInviting = false;
    }

    @Override
    protected void hideProgressCircle() {
        mIsInviting = false;
        mListener.stopProgressAnim();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * <p/>
     * Extends {@link BaseFragmentInteractionListener}.
     */
    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {

        /**
         * Handles the start of the loading animation of the {@link FabProgress}.
         */
        void startProgressAnim();

        /**
         * Handles the start of the final loading animation of the {@link FabProgress}.
         */
        void startFinalProgressAnim();

        /**
         * Handles the action to hide the loading animation of the {@link FabProgress}.
         */
        void stopProgressAnim();
    }

}
