package ch.giantific.qwittig.helper;

import android.app.Activity;
import android.os.Bundle;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import ch.giantific.qwittig.data.parse.models.Installation;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 10.12.14.
 */
public class LogoutHelper extends BaseHelper {

    private HelperInteractionListener mListener;

    public LogoutHelper() {
        // empty default constructor
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

        startProcess();
    }

    void startProcess() {
        handleInstallation();
    }

    final void handleInstallation() {
        // Unsubscribe from all notification channels and remove currentUser from installation
        // object, so that the device does not keep receiving push notifications.
        ParseInstallation installation = Installation.getResetInstallation();
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    onInstallationSaveFailed(e);
                    return;
                }

                onInstallationSaved();
            }
        });
    }

    void onInstallationSaveFailed(ParseException e) {
        if (mListener != null) {
            mListener.onLogoutFailed(e);
        }
    }

    void onInstallationSaved() {
        logOut();
    }

    final void logOut() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                // ignore possible exception, currentUser will always be null now
                onLogoutSucceeded();
            }
        });
    }

    void onLogoutSucceeded() {
        if (mListener != null) {
            mListener.onLogoutSucceeded();
        }
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
