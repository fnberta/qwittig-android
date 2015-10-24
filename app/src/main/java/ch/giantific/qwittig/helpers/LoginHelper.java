package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

import ch.giantific.qwittig.data.parse.models.User;

/**
 * Created by fabio on 10.12.14.
 */
public class LoginHelper extends BaseHelper {

    private static final String BUNDLE_USERNAME = "BUNDLE_USERNAME";
    private static final String BUNDLE_PASSWORD = "BUNDLE_PASSWORD";
    private static final String BUNDLE_NICKNAME = "BUNDLE_NICKNAME";
    private static final String BUNDLE_AVATAR = "BUNDLE_AVATAR";
    private HelperInteractionListener mListener;
    private byte[] mAvatar;

    public LoginHelper() {
        // empty default constructor
    }

    public static LoginHelper newInstance(String username) {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    public static LoginHelper newInstance(String username, String password) {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_USERNAME, username);
        args.putString(BUNDLE_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    public static LoginHelper newInstance(String username, String password, String nickname,
                                          byte[] avatar) {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_USERNAME, username);
        args.putString(BUNDLE_PASSWORD, password);
        args.putString(BUNDLE_NICKNAME, nickname);
        args.putByteArray(BUNDLE_AVATAR, avatar);
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

        String username = "";
        String password = "";
        String nickname = "";
        Bundle args = getArguments();
        if (args != null) {
            username = args.getString(BUNDLE_USERNAME);
            password = args.getString(BUNDLE_PASSWORD);
            nickname = args.getString(BUNDLE_NICKNAME);
            mAvatar = args.getByteArray(BUNDLE_AVATAR);
        }

        if (TextUtils.isEmpty(password)) {
            resetPassword(username);
        } else if (TextUtils.isEmpty(nickname)) {
            loginUser(username, password);
        } else {
            createAccount(username, password, nickname);
        }
    }

    private void resetPassword(String email) {
        ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLoginFailed(e);
                    }
                    return;
                }

                if (mListener != null) {
                    mListener.onPasswordReset();
                }
            }
        });
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLoginFailed(e);
                    }
                    return;
                }

                if (parseUser != null && mListener != null) {
                    mListener.onLoggedIn(parseUser);
                }
            }
        });
    }

    private void createAccount(String username, String password, String nickname) {
        // if avatar is null, don't set one in parse.com
        if (mAvatar != null) {
            User user = new User(username, password, nickname, mAvatar);
            signUpUser(user, username, password);
        } else {
            User user = new User(username, password, nickname);
            signUpUser(user, username, password);
        }
    }

    private void signUpUser(User user, final String email, final String password) {
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLoginFailed(e);
                    }
                    return;
                }

                loginUser(email, password);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface HelperInteractionListener {
        void onLoginFailed(ParseException e);

        void onLoggedIn(ParseUser parseUser);

        void onPasswordReset();
    }
}
