package ch.giantific.qwittig.helper;

import android.app.Activity;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.Map;

import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 10.12.14.
 */
public class DeleteAccountHelper extends LogoutHelper {

    private HelperInteractionListener mListener;

    public DeleteAccountHelper() {
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
    void startProcess() {
        deleteAccount();
    }

    private void deleteAccount() {
        Map<String, Object> params = new HashMap<>();
        ParseCloud.callFunctionInBackground(CloudCode.DELETE_ACCOUNT, params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onDeleteUserFailed(e);
                    }
                    return;
                }

                handleInstallation();
            }
        });
    }

    @Override
    protected void onInstallationSaveFailed(ParseException e) {
        if (mListener != null) {
            mListener.onDeleteUserFailed(e);
        }
    }

    @Override
    protected void onInstallationSaved() {
        deleteUser();
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
                        mListener.onDeleteUserFailed(e);
                    }
                    return;
                }

                logOut();
            }
        });

        // TODO: check in CloudCode if user is the only member in his groups, if yes delete groups (tricky!!)
    }

    @Override
    protected void onLogoutSucceeded() {
        if (mListener != null) {
            mListener.onLogoutSucceeded();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener extends LogoutHelper.HelperInteractionListener {
        void onDeleteUserFailed(ParseException e);
    }
}
