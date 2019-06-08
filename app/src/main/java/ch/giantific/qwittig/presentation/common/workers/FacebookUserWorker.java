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
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import rx.Observable;

/**
 * Handles Google login sign out and unlink.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public class FacebookUserWorker extends BaseWorker<Void, GoogleUserWorkerListener> {

    private static final String WORKER_TAG = FacebookUserWorker.class.getCanonicalName();
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_EMAIL = "EMAIL";
    private static final String KEY_PASSWORD = "PASSWORD";
    private static final String KEY_TOKEN = "TOKEN";
    @Inject
    Application appContext;

    public FacebookUserWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link FacebookUserWorker} that un-links the user from his google
     * accounts and optionally deletes the user as well.
     *
     * @return a new instance of {@link FacebookUserWorker}
     */
    public static FacebookUserWorker attachDelete(@NonNull FragmentManager fm,
                                                  @NonNull String token) {
        FacebookUserWorker worker = (FacebookUserWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new FacebookUserWorker();
            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, FacebookUserAction.DELETE);
            args.putString(KEY_TOKEN, token);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    public static FacebookUserWorker attachUnlink(@NonNull FragmentManager fm,
                                                  @NonNull String email,
                                                  @NonNull String password,
                                                  @NonNull String token) {
        FacebookUserWorker worker = (FacebookUserWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new FacebookUserWorker();
            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, FacebookUserAction.UNLINK);
            args.putString(KEY_EMAIL, email);
            args.putString(KEY_PASSWORD, password);
            args.putString(KEY_TOKEN, token);
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
        @FacebookUserAction
        final int type = args.getInt(KEY_TYPE);
        final FirebaseUser firebaseUser = userRepo.getCurrentUser();
        if (firebaseUser == null) {
            return null;
        }

        switch (type) {
            case FacebookUserAction.DELETE: {
                final String token = args.getString(KEY_TOKEN, "");
                final AuthCredential authCredential = FacebookAuthProvider.getCredential(token);
                return userRepo.unlinkFacebook()
                        .flatMap(aVoid -> userRepo.deleteUser(firebaseUser, authCredential))
                        .toObservable();
            }
            case FacebookUserAction.UNLINK: {
                final String email = args.getString(KEY_EMAIL, "");
                final String password = args.getString(KEY_PASSWORD, "");
                final String token = args.getString(KEY_TOKEN, "");
                final AuthCredential oldCredential = FacebookAuthProvider.getCredential(token);
                final AuthCredential newCredential = EmailAuthProvider.getCredential(email, password);
                return userRepo.linkUserWithCredential(firebaseUser, oldCredential, newCredential)
                        .flatMap(authResult -> userRepo.unlinkFacebook())
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

    @IntDef({FacebookUserAction.UNLINK, FacebookUserAction.DELETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FacebookUserAction {
        int UNLINK = 1;
        int DELETE = 2;
    }
}
