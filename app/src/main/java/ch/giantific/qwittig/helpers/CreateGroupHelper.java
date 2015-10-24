package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 10.12.14.
 */
public class CreateGroupHelper extends BaseInviteHelper {

    private static final String BUNDLE_GROUP_NAME = "BUNDLE_GROUP_NAME";
    private static final String BUNDLE_GROUP_CURRENCY = "BUNDLE_GROUP_CURRENCY";
    private static final String BUNDLE_USERS_TO_INVITE = "BUNDLE_USERS_TO_INVITE";
    private static final String LOG_TAG = CreateGroupHelper.class.getSimpleName();
    private HelperInteractionListener mListener;
    private List<String> mUsersToInvite;

    public CreateGroupHelper() {
        // empty default constructor
    }

    public static CreateGroupHelper newInstance(String groupName, String groupCurrency,
                                                ArrayList<String> usersToInvite) {
        CreateGroupHelper fragment = new CreateGroupHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_GROUP_NAME, groupName);
        args.putString(BUNDLE_GROUP_CURRENCY, groupCurrency);
        args.putStringArrayList(BUNDLE_USERS_TO_INVITE, usersToInvite);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelperInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
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

        if (TextUtils.isEmpty(groupName)) {
            return;
        }

        createNewGroup(groupName, groupCurrency);
    }

    private void createNewGroup(final String groupName, String groupCurrency) {
        final User currentUser = (User) ParseUser.getCurrentUser();
        final Group groupOld = currentUser.getCurrentGroup();
        final Group groupNew = new Group(groupName, groupCurrency);
        currentUser.addGroup(groupNew);
        currentUser.setCurrentGroup(groupNew);
        // We use saveInBackground because we need the object to have an objectId when
        // SettingsFragment starts
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
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

    public interface HelperInteractionListener {
        void onNewGroupCreated(Group newGroup, boolean invitingUser);

        void onCreateNewGroupFailed(ParseException e);

        void onUsersInvited();

        void onInviteUsersFailed(ParseException e);
    }
}
