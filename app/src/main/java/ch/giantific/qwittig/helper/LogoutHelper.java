package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;

import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Installation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.Utils;

/**
 * Created by fabio on 10.12.14.
 */
public class LogoutHelper extends Fragment {

    private static final String BUNDLE_DELETE_USER = "delete_user";
    private HelperInteractionListener mListener;
    private boolean mDeleteUser;

    public LogoutHelper() {
        // empty default constructor
    }

    public static LogoutHelper newInstance(boolean deleteUser) {
        LogoutHelper fragment = new LogoutHelper();
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_DELETE_USER, deleteUser);
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

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        Bundle args = getArguments();
        if (args != null) {
            mDeleteUser = args.getBoolean(BUNDLE_DELETE_USER);
        }

        logOutUser();
    }

    private void logOutUser() {
        // Unsubscribe from all notification channels and remove currentUser from installation
        // object, so that the device does not keep receiving push notifications.
        ParseInstallation installation = Installation.getResetInstallation();
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLogoutFailed(e);
                    }
                    return;
                }

                if (mDeleteUser) {
                    deleteUser();
                } else {
                    logOut();
                }
            }
        });
    }

    private void deleteUser() {
        final User currentUser = (User) ParseUser.getCurrentUser();
        final String username = currentUser.getUsername();
        currentUser.deleteUserFields();
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    currentUser.undeleteUserFields(username);

                    if (mListener != null) {
                        mListener.onLogoutFailed(e);
                    }
                    return;
                }

                logOut();
            }
        });

        // TODO: check in CloudCode if user is the only member in his groups, if yes delete groups (tricky!!)
    }

    private void logOut() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                // ignore possible exception, currentUser will always be null now
                if (mListener != null) {
                    mListener.onLogoutSucceeded();
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
        void onLogoutFailed(ParseException e);
        void onLogoutSucceeded();
    }
}
