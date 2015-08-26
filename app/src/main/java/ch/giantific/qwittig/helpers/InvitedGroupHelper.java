package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
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

import ch.giantific.qwittig.PushBroadcastReceiver;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 10.12.14.
 */
public class InvitedGroupHelper extends BaseHelper {

    private static final String GROUP_ID = "group_id";
    private static final String LOG_TAG = InvitedGroupHelper.class.getSimpleName();
    private HelperInteractionListener mListener;
    private String mGroupId;

    public InvitedGroupHelper() {
        // empty default constructor
    }

    public static InvitedGroupHelper newInstance(String groupId) {
        InvitedGroupHelper fragment = new InvitedGroupHelper();
        Bundle args = new Bundle();
        args.putString(GROUP_ID, groupId);
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

        Bundle args = getArguments();
        if (args != null) {
            mGroupId = args.getString(GROUP_ID);
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
            public void done(Object o, ParseException e) {
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
            public void done(ParseObject parseObject, ParseException e) {
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
            public void done(ParseException e) {
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

    public interface HelperInteractionListener {
        void onInvitedGroupQueried(ParseObject parseObject);

        void onInvitedGroupQueryFailed(ParseException e);

        void onEmailNotValid();

        void onUserJoinedGroup();

        void onUserJoinGroupFailed(ParseException e);
    }
}
