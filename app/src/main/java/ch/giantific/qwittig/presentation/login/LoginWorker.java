/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;
import rx.functions.Func1;

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
public class LoginWorker extends BaseWorker<User, LoginWorkerListener> {

    private static final String WORKER_TAG = LoginWorker.class.getCanonicalName();
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_PASSWORD = "PASSWORD";
    private static final String KEY_IDENTITY_ID = "IDENTITY_ID";
    private static final String KEY_GOOGLE_ID_TOKEN = "ID_TOKEN";
    private static final String KEY_GOOGLE_PHOTO_URL = "GOOGLE_PHOTO_URL";
    @Type
    private int mType;

    public LoginWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link LoginWorker} with a username and a password as arguments.
     * This will log in the user.
     *
     * @param fm         the fragment manager to user for the transaction
     * @param username   the name of the user
     * @param password   the password of the user
     * @param identityId the identity id the user is invited to, empty if no invite is pending
     * @return a new instance of {@link LoginWorker} that will log in the user
     */
    public static LoginWorker attachEmailLoginInstance(@NonNull FragmentManager fm,
                                                       @NonNull String username,
                                                       @NonNull String password,
                                                       @NonNull String identityId) {
        LoginWorker worker = (LoginWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LoginWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, Type.LOGIN_EMAIL);
            args.putString(KEY_USERNAME, username);
            args.putString(KEY_PASSWORD, password);
            args.putString(KEY_IDENTITY_ID, identityId);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    /**
     * Returns a new instance of {@link LoginWorker} with a username as an argument. This will
     * reset the password of the user.
     *
     * @param fm       the fragment manager to user for the transaction
     * @param username the name of the user
     * @return a new instance of {@link LoginWorker} that will reset the password of the user
     */
    @NonNull
    public static LoginWorker attachResetPasswordInstance(@NonNull FragmentManager fm,
                                                          @NonNull String username) {
        LoginWorker worker = (LoginWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LoginWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, Type.RESET_PASSWORD);
            args.putString(KEY_USERNAME, username);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    /**
     * Returns a new instance of {@link LoginWorker} with a username, a password, a nickname and
     * optionally an avatar image. This will create a new account for the user and then log him/her
     * in.
     *
     * @param fm         the fragment manager to user for the transaction
     * @param username   the name of the user
     * @param password   the password of the user
     * @param identityId the identity id the user is invited to, empty if no invite is pending
     * @return a new instance of {@link LoginWorker} that will create a new account for the user
     * and log him/her in
     */
    @NonNull
    public static LoginWorker attachEmailSignUpInstance(@NonNull FragmentManager fm,
                                                        @NonNull String username,
                                                        @NonNull String password,
                                                        @NonNull String identityId) {
        LoginWorker worker = (LoginWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LoginWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, Type.SIGN_UP_EMAIL);
            args.putString(KEY_USERNAME, username);
            args.putString(KEY_PASSWORD, password);
            args.putString(KEY_IDENTITY_ID, identityId);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();

        }
        return worker;
    }

    /**
     * Returns a new instance of {@link LoginWorker} that logs in the user with his facebook
     * credentials. If the login was successful, sets the email address and profile image the user
     * has set in facebook.
     *
     * @param fm         the fragment manager to user for the transaction
     * @param identityId the identity id the user is invited to, empty if no invite is pending
     * @return a new instance of {@link LoginWorker} that logs in the user with facebook
     */
    public static LoginWorker attachFacebookLoginInstance(@NonNull FragmentManager fm,
                                                          @NonNull String identityId) {
        LoginWorker worker = (LoginWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LoginWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, Type.LOGIN_FACEBOOK);
            args.putString(KEY_IDENTITY_ID, identityId);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();

        }
        return worker;
    }

    /**
     * Returns a new instance of {@link LoginWorker} that logs in the user with his Google
     * credentials. Sends the idToken received form GoogleApiClient to the server where it gets
     * verified and the user gets logged in (or a new account is created).
     *
     * @param idToken    the token received from GoogleApiClient
     * @param identityId the identity id the user is invited to, empty if no invite is pending
     * @return a new instance of {@link LoginWorker} that logs in the user with Google
     */
    public static LoginWorker attachGoogleVerifyTokenInstance(@NonNull FragmentManager fm,
                                                              @Nullable String idToken,
                                                              @Nullable String displayName,
                                                              @Nullable Uri photoUrl,
                                                              @NonNull String identityId) {
        LoginWorker worker = (LoginWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LoginWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, Type.LOGIN_GOOGLE);
            args.putString(KEY_GOOGLE_ID_TOKEN, idToken);
            args.putString(KEY_USERNAME, displayName);
            args.putString(KEY_GOOGLE_PHOTO_URL, photoUrl != null ? photoUrl.toString() : "");
            args.putString(KEY_IDENTITY_ID, identityId);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();

        }
        return worker;
    }

    @Override
    protected void injectWorkerDependencies(@NonNull WorkerComponent component) {
        component.inject(this);
    }

    @SuppressWarnings("WrongConstant")
    @Nullable
    @Override
    protected Observable<User> getObservable(@NonNull Bundle args) {
        mType = args.getInt(KEY_TYPE, 0);
        switch (mType) {
            case Type.LOGIN_EMAIL: {
                final String username = args.getString(KEY_USERNAME, "");
                final String password = args.getString(KEY_PASSWORD, "");
                final String identityId = args.getString(KEY_IDENTITY_ID, "");
                return mUserRepo.loginEmail(username, password, identityId).toObservable();
            }
            case Type.RESET_PASSWORD: {
                final String username = args.getString(KEY_USERNAME, "");
                return mUserRepo.requestPasswordReset(username)
                        .map(new Func1<String, User>() {
                            @Override
                            public User call(String s) {
                                return mUserRepo.getCurrentUser();
                            }
                        })
                        .toObservable();
            }
            case Type.SIGN_UP_EMAIL: {
                final String username = args.getString(KEY_USERNAME, "");
                final String password = args.getString(KEY_PASSWORD, "");
                final String identityId = args.getString(KEY_IDENTITY_ID, "");
                return mUserRepo.signUpEmail(username, password, identityId).toObservable();
            }
            case Type.LOGIN_FACEBOOK: {
                final String identityId = args.getString(KEY_IDENTITY_ID, "");
                return mUserRepo.loginFacebook(this, identityId).toObservable();
            }
            case Type.LOGIN_GOOGLE: {
                final String username = args.getString(KEY_USERNAME, "");
                final String idToken = args.getString(KEY_GOOGLE_ID_TOKEN, "");
                final Uri photoUrl = Uri.parse(args.getString(KEY_GOOGLE_PHOTO_URL, ""));
                final String identityId = args.getString(KEY_IDENTITY_ID, "");
                return mUserRepo.loginGoogle(idToken, username, photoUrl, identityId, this).toObservable();
            }
        }

        return null;
    }

    @Override
    protected void onError() {
        mActivity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<User> observable) {
        mActivity.setUserLoginStream(observable.toSingle(), WORKER_TAG, mType);
    }

    @IntDef({Type.LOGIN_EMAIL, Type.LOGIN_FACEBOOK, Type.LOGIN_GOOGLE, Type.SIGN_UP_EMAIL,
            Type.RESET_PASSWORD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int LOGIN_EMAIL = 1;
        int LOGIN_FACEBOOK = 2;
        int LOGIN_GOOGLE = 3;
        int SIGN_UP_EMAIL = 4;
        int RESET_PASSWORD = 5;
    }
}
