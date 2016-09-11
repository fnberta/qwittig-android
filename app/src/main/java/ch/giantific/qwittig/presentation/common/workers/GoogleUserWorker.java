/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.workers;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import rx.Observable;
import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Handles Google login sign out and unlink.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class GoogleUserWorker extends BaseWorker<Void, GoogleUserWorkerListener> {

    private static final String WORKER_TAG = GoogleUserWorker.class.getCanonicalName();
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_EMAIL = "EMAIL";
    private static final String KEY_PASSWORD = "PASSWORD";
    private static final String KEY_ID_TOKEN = "ID_TOKEN";
    @Inject
    Application appContext;

    public GoogleUserWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link GoogleUserWorker} that signs out from the user's google
     * account.
     *
     * @return a new instance of {@link GoogleUserWorker}
     */
    public static GoogleUserWorker attachSignOut(@NonNull FragmentManager fm) {
        GoogleUserWorker worker = (GoogleUserWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new GoogleUserWorker();
            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, GoogleUserAction.SIGN_OUT);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    /**
     * Attaches a new instance of {@link GoogleUserWorker} that un-links the user from his google
     * accounts and optionally deletes the user as well.
     *
     * @return a new instance of {@link GoogleUserWorker}
     */
    public static GoogleUserWorker attachDelete(@NonNull FragmentManager fm,
                                                @NonNull String idToken) {
        GoogleUserWorker worker = (GoogleUserWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new GoogleUserWorker();
            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, GoogleUserAction.DELETE);
            args.putString(KEY_ID_TOKEN, idToken);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    public static GoogleUserWorker attachUnlink(@NonNull FragmentManager fm,
                                                @NonNull String email,
                                                @NonNull String password,
                                                @NonNull String idToken) {
        GoogleUserWorker worker = (GoogleUserWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new GoogleUserWorker();
            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, GoogleUserAction.UNLINK);
            args.putString(KEY_EMAIL, email);
            args.putString(KEY_PASSWORD, password);
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

    @Nullable
    @Override
    protected Observable<Void> getObservable(@NonNull Bundle args) {
        @GoogleUserAction
        final int type = args.getInt(KEY_TYPE);
        final FirebaseUser firebaseUser = userRepo.getCurrentUser();
        if (firebaseUser == null) {
            return null;
        }

        switch (type) {
            case GoogleUserAction.SIGN_OUT: {
                return userRepo.signOutGoogle(appContext)
                        .doOnSuccess(aVoid -> userRepo.signOut(firebaseUser))
                        .toObservable();
            }
            case GoogleUserAction.DELETE: {
                final String idToken = args.getString(KEY_ID_TOKEN, "");
                final AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
                return userRepo.unlinkGoogle(appContext, firebaseUser, authCredential)
                        .flatMap(aVoid -> userRepo.deleteUser(firebaseUser, authCredential))
                        .toObservable();
            }
            case GoogleUserAction.UNLINK: {
                final String email = args.getString(KEY_EMAIL, "");
                final String password = args.getString(KEY_PASSWORD, "");
                final String idToken = args.getString(KEY_ID_TOKEN, "");
                final AuthCredential oldCredential = GoogleAuthProvider.getCredential(idToken, null);
                final AuthCredential newCredential = EmailAuthProvider.getCredential(email, password);
                return userRepo.linkUserWithCredential(firebaseUser, oldCredential, newCredential)
                        .flatMap(authResult -> userRepo.unlinkGoogle(appContext, firebaseUser, oldCredential))
                        .toObservable();
            }
            default:
                throw new RuntimeException("This worker does not support the selected type: " + type);
        }
    }

    @Override
    protected void onError() {
        activity.onWorkerError(WORKER_TAG);
    }

    @Override
    protected void setStream(@NonNull Observable<Void> observable) {
        activity.setGoogleUserStream(observable.toSingle(), WORKER_TAG);
    }

    @IntDef({GoogleUserAction.SIGN_OUT, GoogleUserAction.UNLINK, GoogleUserAction.DELETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GoogleUserAction {
        int SIGN_OUT = 1;
        int UNLINK = 2;
        int DELETE = 3;
    }
}
