/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.google.firebase.auth.FirebaseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import ch.giantific.qwittig.presentation.common.workers.BaseWorker;
import rx.Observable;

/**
 * Handles the different use-cases connected with the account of a user (log-in, create account)
 * <p>
 * Has three specific purposes:
 * <ol>
 * <li>Log in a user</li>
 * <li>Create a new account and then log him/she in</li>
 * </ol>
 */
public class LoginWorker extends BaseWorker<FirebaseUser, LoginWorkerListener> {

    private static final String WORKER_TAG = LoginWorker.class.getCanonicalName();
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_PASSWORD = "PASSWORD";
    private static final String KEY_ID_TOKEN = "ID_TOKEN";
    @LoginType
    private int type;

    public LoginWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link LoginWorker} with a username and a password as arguments.
     * This will log in the user.
     *
     * @param fm       the fragment manager to user for the transaction
     * @param username the name of the user
     * @param password the password of the user
     * @return a new instance of {@link LoginWorker} that will log in the user
     */
    public static LoginWorker attachEmailLoginInstance(@NonNull FragmentManager fm,
                                                       @NonNull String username,
                                                       @NonNull String password) {
        LoginWorker worker = (LoginWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LoginWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, LoginType.LOGIN_EMAIL);
            args.putString(KEY_USERNAME, username);
            args.putString(KEY_PASSWORD, password);
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
     * @param fm       the fragment manager to user for the transaction
     * @param username the name of the user
     * @param password the password of the user
     * @return a new instance of {@link LoginWorker} that will create a new account for the user
     * and log him/her in
     */
    @NonNull
    public static LoginWorker attachEmailSignUpInstance(@NonNull FragmentManager fm,
                                                        @NonNull String username,
                                                        @NonNull String password) {
        LoginWorker worker = (LoginWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LoginWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, LoginType.SIGN_UP_EMAIL);
            args.putString(KEY_USERNAME, username);
            args.putString(KEY_PASSWORD, password);
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
     * @param fm the fragment manager to user for the transaction
     * @return a new instance of {@link LoginWorker} that logs in the user with facebook
     */
    public static LoginWorker attachFacebookLoginInstance(@NonNull FragmentManager fm,
                                                          @NonNull String idToken) {
        LoginWorker worker = (LoginWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LoginWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, LoginType.LOGIN_FACEBOOK);
            args.putString(KEY_ID_TOKEN, idToken);
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
     * @param idToken the token received from GoogleApiClient
     * @return a new instance of {@link LoginWorker} that logs in the user with Google
     */
    public static LoginWorker attachGoogleLoginInstance(@NonNull FragmentManager fm,
                                                        @Nullable String idToken) {
        LoginWorker worker = (LoginWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new LoginWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, LoginType.LOGIN_GOOGLE);
            args.putString(KEY_ID_TOKEN, idToken);
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
    protected Observable<FirebaseUser> getObservable(@NonNull Bundle args) {
        type = args.getInt(KEY_TYPE, 0);
        switch (type) {
            case LoginType.LOGIN_EMAIL: {
                final String username = args.getString(KEY_USERNAME, "");
                final String password = args.getString(KEY_PASSWORD, "");
                return userRepo.loginEmail(username, password).toObservable();
            }
            case LoginType.SIGN_UP_EMAIL: {
                final String username = args.getString(KEY_USERNAME, "");
                final String password = args.getString(KEY_PASSWORD, "");
                return userRepo.signUpEmail(username, password).toObservable();
            }
            case LoginType.LOGIN_FACEBOOK: {
                final String idToken = args.getString(KEY_ID_TOKEN, "");
                return userRepo.loginFacebook(idToken).toObservable();
            }
            case LoginType.LOGIN_GOOGLE: {
                final String idToken = args.getString(KEY_ID_TOKEN, "");
                return userRepo.loginGoogle(idToken).toObservable();
            }
        }

        return null;
    }

    @Override
    protected void onError() {
        activity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<FirebaseUser> observable) {
        activity.setUserLoginStream(observable.toSingle(), WORKER_TAG, type);
    }

    @IntDef({LoginType.LOGIN_EMAIL, LoginType.LOGIN_FACEBOOK, LoginType.LOGIN_GOOGLE, LoginType.SIGN_UP_EMAIL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoginType {
        int LOGIN_EMAIL = 1;
        int LOGIN_FACEBOOK = 2;
        int LOGIN_GOOGLE = 3;
        int SIGN_UP_EMAIL = 4;
    }
}
