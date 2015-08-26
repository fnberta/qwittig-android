package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabio on 10.12.14.
 */
public class InviteUsersHelper extends BaseInviteHelper {

    private static final String USERS_TO_INVITE = "users_to_invite";
    private static final String GROUP_NAME = "group_name";
    private static final String LOG_TAG = InviteUsersHelper.class.getSimpleName();
    private HelperInteractionListener mListener;

    public InviteUsersHelper() {
        // empty default constructor
    }

    public static InviteUsersHelper newInstance(ArrayList<String> usersToInvite, String groupName) {
        InviteUsersHelper fragment = new InviteUsersHelper();
        Bundle args = new Bundle();
        args.putStringArrayList(USERS_TO_INVITE, usersToInvite);
        args.putString(GROUP_NAME, groupName);
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

        List<String> usersToInvite = null;
        String groupName = "";
        Bundle args = getArguments();
        if (args != null) {
            usersToInvite = args.getStringArrayList(USERS_TO_INVITE);
            groupName = args.getString(GROUP_NAME);
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

    public interface HelperInteractionListener {
        void onUsersInvited();

        void onInviteUsersFailed(ParseException e);
    }
}
