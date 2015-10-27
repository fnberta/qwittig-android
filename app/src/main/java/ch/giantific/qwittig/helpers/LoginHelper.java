/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

import ch.giantific.qwittig.data.parse.models.User;

/**
 * Handles the different use-cases connected with the account of a user (log-in, create account,
 * reset passowrd)
 * <p/>
 * Has three specific purposes:
 * <ol>
 * <li>Log in a user</li>
 * <li>Create a new account and then log him/she in</li>
 * <li>Reset the password of a user</li>
 * </ol>
 */
public class LoginHelper extends BaseHelper {

    private static final String BUNDLE_USERNAME = "BUNDLE_USERNAME";
    private static final String BUNDLE_PASSWORD = "BUNDLE_PASSWORD";
    private static final String BUNDLE_NICKNAME = "BUNDLE_NICKNAME";
    private static final String BUNDLE_AVATAR = "BUNDLE_AVATAR";
    @Nullable
    private HelperInteractionListener mListener;
    @Nullable
    private byte[] mAvatar;

    public LoginHelper() {
        // empty default constructor
    }

    /**
     * Returns a new instance of {@link LoginHelper} with a username as an argument. This will
     * reset the password of the user.
     *
     * @param username the name of the user
     * @return a new instance of {@link LoginHelper} that will reset the password of the user
     */
    @NonNull
    public static LoginHelper newInstance(@NonNull String username) {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns a new instance of {@link LoginHelper} with a username and a password as arguments.
     * This will log in the user.
     *
     * @param username the name of the user
     * @param password the password of the user
     * @return a new instance of {@link LoginHelper} that will log in the user
     */
    @NonNull
    public static LoginHelper newInstance(@NonNull String username, @NonNull String password) {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putString(BUNDLE_USERNAME, username);
        args.putString(BUNDLE_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns a new instance of {@link LoginHelper} with a username, a password, a nickname and
     * optionally an avatar image. This will create a new account for the user and then log him/her
     * in.
     *
     * @param username the name of the user
     * @param password the password of the user
     * @param nickname the nickname of the user
     * @param avatar   the avatar image of the user
     * @return a new instance of {@link LoginHelper} that will create a new account for the user
     * and log him/her in
     */
    @NonNull
    public static LoginHelper newInstance(@NonNull String username, @NonNull String password,
                                          @NonNull String nickname, @Nullable byte[] avatar) {
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

        final boolean usernameIsEmpty = TextUtils.isEmpty(username);
        final boolean passwordIsEmpty = TextUtils.isEmpty(password);
        final boolean nicknameIsEmpty = TextUtils.isEmpty(nickname);

        if (!usernameIsEmpty && passwordIsEmpty) {
            resetPassword(username);
        } else if (!usernameIsEmpty && nicknameIsEmpty) {
            loginUser(username, password);
        } else if (!usernameIsEmpty) {
            createAccount(username, password, nickname);
        }
    }

    private void resetPassword(@NonNull String email) {
        ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
            @Override
            public void done(@Nullable ParseException e) {
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

    private void loginUser(@NonNull String username, @NonNull String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(@Nullable ParseUser parseUser, @Nullable ParseException e) {
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

    private void createAccount(@NonNull String username, @NonNull String password,
                               @NonNull String nickname) {
        // if avatar is null, don't set one in parse.com
        if (mAvatar != null) {
            User user = new User(username, password, nickname, mAvatar);
            signUpUser(user, username, password);
        } else {
            User user = new User(username, password, nickname);
            signUpUser(user, username, password);
        }
    }

    private void signUpUser(@NonNull User user, @NonNull final String email,
                            @NonNull final String password) {
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(@Nullable ParseException e) {
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

    /**
     * Defines the actions to take after a successful login, a failed login or the reset of a
     * password.
     */
    public interface HelperInteractionListener {
        /**
         * Handles successful login of a user
         *
         * @param user the now logged {@link User} object
         */
        void onLoggedIn(@NonNull ParseUser user);

        /**
         * Handles the failure to log in a user
         *
         * @param e the {@link ParseException} thrown during the process
         */
        void onLoginFailed(@NonNull ParseException e);

        /**
         * Handles the case when the link to reset a password was successfully sent.
         */
        void onPasswordReset();
    }
}
