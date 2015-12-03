/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.data.helpers.account;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.DeleteCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helpers.BaseHelper;
import ch.giantific.qwittig.data.rest.CloudCodeClient;
import ch.giantific.qwittig.domain.models.Avatar;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.fragments.dialogs.EmailPromptDialogFragment;

/**
 * Handles the different use-cases connected with the account of a user (log-in, create account,
 * reset password)
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
    private static final String BUNDLE_ID_TOKEN = "BUNDLE_ID_TOKEN";
    private static final String BUNDLE_TYPE = "BUNDLE_TYPE";
    private static final int TYPE_LOGIN_EMAIL = 1;
    private static final int TYPE_LOGIN_FACEBOOK = 2;
    private static final int TYPE_LOGIN_GOOGLE = 3;
    private static final int TYPE_SIGN_UP = 4;
    private static final int TYPE_RESET_PASSWORD = 5;
    private static final String LOG_TAG = LoginHelper.class.getSimpleName();
    private static final String PROMPT_EMAIL_DIALOG = "PROMPT_EMAIL_DIALOG";
    @Nullable
    private HelperInteractionListener mListener;
    private User mFacebookUser;

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
    public static LoginHelper newInstanceResetPassword(@NonNull String username) {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_TYPE, TYPE_RESET_PASSWORD);
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
    public static LoginHelper newInstanceLogin(@NonNull String username, @NonNull String password) {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_TYPE, TYPE_LOGIN_EMAIL);
        args.putString(BUNDLE_USERNAME, username);
        args.putString(BUNDLE_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns a new instance of {@link LoginHelper} that logs in the user with his facebook
     * credentials. If the login was successful, sets the email address and profile image the user
     * has set in facebook.
     *
     * @return a new instance of {@link LoginHelper} that logs in the user with facebook
     */
    public static LoginHelper newInstanceFacebookLogin() {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_TYPE, TYPE_LOGIN_FACEBOOK);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Returns a new instance of {@link LoginHelper} that logs in the user with his Google
     * credentials. Sends the idToken received form GoogleApiClient to the server where it gets
     * verified and the user gets logged in (or a new account is created).
     *
     * @param idToken the token received from GoogleApiClient
     * @return a new instance of {@link LoginHelper} that logs in the user with Google
     */
    public static LoginHelper newInstanceGoogleLogin(@NonNull String idToken,
                                                     @NonNull String displayName,
                                                     @NonNull Uri photoUrl) {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_TYPE, TYPE_LOGIN_GOOGLE);
        args.putString(BUNDLE_ID_TOKEN, idToken);
        args.putString(BUNDLE_USERNAME, displayName);
        args.putString(BUNDLE_AVATAR, photoUrl.toString());
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
    public static LoginHelper newInstanceSignUp(@NonNull String username, @NonNull String password,
                                                @NonNull String nickname, @Nullable byte[] avatar) {
        LoginHelper fragment = new LoginHelper();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_TYPE, TYPE_SIGN_UP);
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

        Bundle args = getArguments();
        if (args == null) {
            if (mListener != null) {
                mListener.onLoginFailed(0);
            }

            return;
        }

        final int type = args.getInt(BUNDLE_TYPE);
        switch (type) {
            case TYPE_LOGIN_EMAIL: {
                final String username = args.getString(BUNDLE_USERNAME, "");
                final String password = args.getString(BUNDLE_PASSWORD, "");
                loginWithEmail(username, password);
                break;
            }
            case TYPE_LOGIN_FACEBOOK: {
                loginWithFacebook();
                break;
            }
            case TYPE_LOGIN_GOOGLE: {
                final String idToken = args.getString(BUNDLE_ID_TOKEN, "");
                final String username = args.getString(BUNDLE_USERNAME, "");
                final Uri photoUrl = Uri.parse(args.getString(BUNDLE_AVATAR, ""));
                loginWithGoogle(idToken, username, photoUrl);
                break;
            }
            case TYPE_SIGN_UP: {
                final String username = args.getString(BUNDLE_USERNAME, "");
                final String password = args.getString(BUNDLE_PASSWORD, "");
                final String nickname = args.getString(BUNDLE_NICKNAME, "");
                final byte[] avatar = args.getByteArray(BUNDLE_AVATAR);
                signUp(username, password, nickname, avatar);
                break;
            }
            case TYPE_RESET_PASSWORD: {
                final String username = args.getString(BUNDLE_USERNAME, "");
                resetPassword(username);
                break;
            }
            default:
                if (mListener != null) {
                    mListener.onLoginFailed(0);
                }
        }
    }

    private void resetPassword(@NonNull String email) {
        ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLoginFailed(e.getCode());
                    }
                    return;
                }

                if (mListener != null) {
                    mListener.onPasswordReset();
                }
            }
        });
    }

    private void loginWithEmail(@NonNull String username, @NonNull String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, @Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLoginFailed(e.getCode());
                    }
                    return;
                }

                if (mListener != null) {
                    mListener.onLoggedIn(parseUser);
                }
            }
        });
    }

    private void signUp(@NonNull final String username, final @NonNull String password,
                        @NonNull String nickname, @Nullable byte[] avatar) {
        User user = new User(username, password, nickname);
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(@Nullable ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLoginFailed(e.getCode());
                    }
                    return;
                }

                loginWithEmail(username, password);
            }
        });
    }

    private void loginWithFacebook() {
        List<String> permissions = Arrays.asList("public_profile", "email");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(getActivity(), permissions, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    if (mListener != null) {
                        mListener.onLoginFailed(err != null ? err.getCode() : 0);
                    }
                    return;
                }

                mFacebookUser = (User) user;
                if (user.isNew()) {
                    setFacebookUserData();
                } else if (mListener != null) {
                    mListener.onLoggedIn(user);
                }
            }
        });
    }

    private void setFacebookUserData() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        if (object != null) {
                            setFacebookUserProfileImage(object);
                        } else {
                            promptForEmail();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,first_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void setFacebookUserProfileImage(final JSONObject facebookData) {
        final String id = facebookData.optString("id");
        final String pictureUrl = "http://graph.facebook.com/" + id + "/picture?type=large";
        if (!TextUtils.isEmpty(pictureUrl)) {
            Glide.with(this)
                    .load(pictureUrl)
                    .asBitmap()
                    .toBytes(Bitmap.CompressFormat.JPEG, Avatar.JPEG_COMPRESSION_RATE)
                    .centerCrop()
                    .into(new SimpleTarget<byte[]>(Avatar.WIDTH, Avatar.HEIGHT) {
                        @Override
                        public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                            mFacebookUser.setAvatar(resource);
                            setFacebookUserProfile(facebookData);
                        }
                    });
        } else {
            setFacebookUserProfile(facebookData);
        }
    }

    private void setFacebookUserProfile(final JSONObject facebookData) {
        mFacebookUser.setDeleted(false);
        mFacebookUser.setStoresFavorites(mFacebookUser.getDefaultStores());

        final String name = facebookData.optString("first_name");
        if (!TextUtils.isEmpty(name)) {
            mFacebookUser.setNickname(name);
        }

        final String email = facebookData.optString("email");
        if (!TextUtils.isEmpty(email)) {
            mFacebookUser.setUsername(email);
            saveThirdPartyLoginUser(mFacebookUser);
        } else {
            promptForEmail();
        }
    }

    private void saveThirdPartyLoginUser(@NonNull final User user) {
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    deleteLoginUser(user);

                    if (mListener != null) {
                        mListener.onLoginFailed(e.getCode());
                    }
                    return;
                }

                if (mListener != null) {
                    mListener.onLoggedIn(user);
                }
            }
        });
    }

    private void promptForEmail() {
        EmailPromptDialogFragment dialog = EmailPromptDialogFragment.newInstance(
                R.string.dialog_login_email_title,
                R.string.dialog_login_email_message,
                android.R.string.yes);
        dialog.show(getFragmentManager(), PROMPT_EMAIL_DIALOG);
    }

    /**
     * Sets the email to the user logged in with facebook and initiates the save of the user.
     *
     * @param email the email to set
     */
    public void onValidEmailSet(String email) {
        mFacebookUser.setUsername(email);
        saveThirdPartyLoginUser(mFacebookUser);
    }

    /**
     * Logs out and deletes the newly created facebook user as no valid email address was entered.
     */
    public void onNoEmailSet() {
        deleteLoginUser(mFacebookUser);
    }

    private void deleteLoginUser(ParseUser user) {
        user.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    // TODO: how to handle this?
                    return;
                }

                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        // ignore possible exception, currentUser will always be null now
                        if (mListener != null) {
                            mListener.onLoginFailed(0);
                        }
                    }
                });
            }
        });
    }

    private void loginWithGoogle(@NonNull String idToken,
                                 @NonNull final String displayName,
                                 @NonNull final Uri photoUrl) {
        final CloudCodeClient cloudCode = new CloudCodeClient();
        cloudCode.loginWithGoogle(idToken, new CloudCodeClient.CloudCodeListener() {
            @Override
            public void onCloudFunctionReturned(Object result) {
                JSONObject token;
                try {
                    token = new JSONObject((String) result);
                } catch (JSONException e) {
                    if (mListener != null) {
                        mListener.onLoginFailed(0);
                    }

                    // TODO: user should also be deleted
                    return;
                }

                final String sessionToken = token.optString("sessionToken");
                final boolean isNew = token.optBoolean("isNew");
                becomeUser(sessionToken, isNew, displayName, photoUrl);
            }

            @Override
            public void onCloudFunctionFailed(int errorCode) {
                // TODO: tell the user why it failed, handle error code appropriately
                if (mListener != null) {
                    mListener.onLoginFailed(errorCode);
                }
            }
        });
    }

    private void becomeUser(@NonNull String sessionToken, final boolean isNew,
                            @NonNull final String displayName,
                            @NonNull final Uri photoUrl) {
        ParseUser.becomeInBackground(sessionToken, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    if (mListener != null) {
                        mListener.onLoginFailed(e.getCode());
                    }
                    return;
                }

                if (isNew) {
                    setGoogleUserProfile((User) user, displayName, photoUrl);
                } else if (mListener != null) {
                    mListener.onLoggedIn(user);
                }
            }
        });
    }

    private void setGoogleUserProfile(@NonNull final User user, @NonNull String displayName,
                                      @NonNull Uri photoUrl) {
        user.setNickname(displayName);
        Glide.with(this)
                .load(photoUrl)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, Avatar.JPEG_COMPRESSION_RATE)
                .centerCrop()
                .into(new SimpleTarget<byte[]>(Avatar.WIDTH, Avatar.HEIGHT) {
                    @Override
                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                        user.setAvatar(resource);
                        saveThirdPartyLoginUser(user);
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
         * @param errorCode the error code of the exception thrown during the process
         */
        void onLoginFailed(int errorCode);

        /**
         * Handles the case when the link to reset a password was successfully sent.
         */
        void onPasswordReset();
    }
}
