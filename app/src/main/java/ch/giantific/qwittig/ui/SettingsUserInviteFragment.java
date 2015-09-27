package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helpers.InviteUsersHelper;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SettingsUserInviteFragment extends SettingsBaseInviteFragment implements
        LocalQuery.UserLocalQueryListener {

    private static final String INVITE_HELPER = "invite_helper";
    private static final String LOG_TAG = SettingsUserInviteFragment.class.getSimpleName();
    private FragmentInteractionListener mListener;
    private LinearLayout mLinearLayoutUsersInvited;
    private TextView mTextViewNoInvitations;
    private List<String> mUsersInvitedEmails = new ArrayList<>();
    private List<View> mUsersInvitedRows = new ArrayList<>();
    private User mCurrentUser;
    private Group mCurrentGroup;
    private boolean mIsInviting;

    public SettingsUserInviteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_user_invite, container, false);

        mLinearLayoutUsersInvited = (LinearLayout) rootView.findViewById(R.id.ll_users_invited);
        mTextViewNoInvitations = (TextView) rootView.findViewById(R.id.tv_no_open_invitations);

        findUsersToInviteViews(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateCurrentUserAndGroup();
        setupInvitedUsers();
        setupUsersToInviteRows();
    }

    private void updateCurrentUserAndGroup() {
        User currentUser = (User) ParseUser.getCurrentUser();
        if (currentUser != null) {
            mCurrentUser = currentUser;
            mCurrentGroup = currentUser.getCurrentGroup();
        }
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
            public void onClick(View v) {
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

        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(getView(), getString(R.string.toast_no_connection));
            return;
        }

        LocalQuery.queryUsers(this);
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
    public void onUsersLocalQueried(List<ParseUser> users) {
        if (allEmailsAreNotAlreadyInGroup(users)) {
            inviteNewUsers();
        }
    }

    private boolean allEmailsAreNotAlreadyInGroup(List<ParseUser> users) {
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
        mListener.progressCircleShow();

        inviteUsersWithHelper(mCurrentGroup);
    }

    private void inviteUsersWithHelper(Group group) {
        FragmentManager fragmentManager = getFragmentManager();
        InviteUsersHelper inviteUsersHelper = findInviteHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (inviteUsersHelper == null) {
            inviteUsersHelper = InviteUsersHelper.newInstance(mUsersToInviteEmails, group.getName());

            fragmentManager.beginTransaction()
                    .add(inviteUsersHelper, INVITE_HELPER)
                    .commit();
        }
    }

    private InviteUsersHelper findInviteHelper(FragmentManager fragmentManager) {
        return (InviteUsersHelper) fragmentManager.findFragmentByTag(INVITE_HELPER);
    }

    private void removeInviteHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        InviteUsersHelper inviteUsersHelper = findInviteHelper(fragmentManager);

        if (inviteUsersHelper != null) {
            fragmentManager.beginTransaction().remove(inviteUsersHelper).commitAllowingStateLoss();
        }
    }

    /**
     * Called from activity when helper fails to invite users
     * @param e
     */
    public void onInviteUsersFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        onParseError(ParseErrorHandler.getErrorMessage(getActivity(), e));
        removeInviteHelper();
    }

    /**
     * Called from activity when helper successfully invited users
     */
    public void onUsersInvited() {
        mListener.progressCircleStartFinal();
        removeInviteHelper();

    }

    public void finishInvitations() {
        setNoInvitationsViewVisibility(false);
        for (String email : mUsersToInviteEmails) {
            addInvitedRow(mUsersInvitedRows.size(), email);
        }
        MessageUtils.showBasicSnackbar(getView(), getString(R.string.toast_user_invited));

        mIsInviting = false;
    }

    @Override
    protected void hideProgressCircle() {
        mIsInviting = false;
        mListener.progressCircleHide();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener extends BaseFragmentInteractionListener {
        void progressCircleShow();

        void progressCircleStartFinal();

        void progressCircleHide();
    }

}
