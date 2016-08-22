/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.workers;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.presentation.common.di.WorkerComponent;
import rx.Observable;

/**
 * Handles the deletion of a user logged in via email and the reset of his password.
 * <p>
 * Subclass of {@link BaseWorker}.
 */
public class EmailUserWorker extends BaseWorker<Void, EmailUserWorkerListener> {

    private static final String WORKER_TAG = EmailUserWorker.class.getCanonicalName();
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_EMAIL_ADDRESS_CURRENT = "EMAIL_ADDRESS_CURRENT";
    private static final String KEY_PASSWORD_CURRENT = "PASSWORD_CURRENT";
    private static final String KEY_EMAIL_ADDRESS_NEW = "EMAIL_ADDRESS_NEW";
    private static final String KEY_PASSWORD_NEW = "PASSWORD_NEW";

    public EmailUserWorker() {
        // empty default constructor
    }

    /**
     * Attaches a new instance of {@link EmailUserWorker} that delete the user's account.
     *
     * @return a new instance of {@link EmailUserWorker}
     */
    @NonNull
    public static EmailUserWorker attachDelete(@NonNull FragmentManager fm,
                                               @NonNull String currentEmail,
                                               @NonNull String currentPassword) {
        EmailUserWorker worker = (EmailUserWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new EmailUserWorker();
            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, EmailUserAction.DELETE);
            args.putString(KEY_EMAIL_ADDRESS_CURRENT, currentEmail);
            args.putString(KEY_PASSWORD_CURRENT, currentPassword);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    /**
     * Returns a new instance of {@link EmailUserWorker} that will reset the password of the user.
     *
     * @param fm    the fragment manager to user for the transaction
     * @param email the email address to send the reset link to
     * @return a new instance of {@link EmailUserWorker} that will reset the password of the user
     */
    @NonNull
    public static EmailUserWorker attachResetPasswordInstance(@NonNull FragmentManager fm,
                                                              @NonNull String email) {
        EmailUserWorker worker = (EmailUserWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new EmailUserWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, EmailUserAction.REQUEST_RESET_PW);
            args.putString(KEY_EMAIL_ADDRESS_CURRENT, email);
            worker.setArguments(args);

            fm.beginTransaction()
                    .add(worker, WORKER_TAG)
                    .commit();
        }

        return worker;
    }

    @NonNull
    public static EmailUserWorker attachChangeEmailPasswordInstance(@NonNull FragmentManager fm,
                                                                    @NonNull String currentEmail,
                                                                    @NonNull String currentPassword,
                                                                    @Nullable String newEmail,
                                                                    @Nullable String newPassword) {
        EmailUserWorker worker = (EmailUserWorker) fm.findFragmentByTag(WORKER_TAG);
        if (worker == null) {
            worker = new EmailUserWorker();

            final Bundle args = new Bundle();
            args.putInt(KEY_TYPE, EmailUserAction.CHANGE_EMAIL_PW);
            args.putString(KEY_EMAIL_ADDRESS_CURRENT, currentEmail);
            args.putString(KEY_PASSWORD_CURRENT, currentPassword);
            args.putString(KEY_EMAIL_ADDRESS_NEW, newEmail);
            args.putString(KEY_PASSWORD_NEW, newPassword);
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
        final FirebaseUser firebaseUser = userRepo.getCurrentUser();
        if (firebaseUser == null) {
            return null;
        }

        @EmailUserAction
        final int type = args.getInt(KEY_TYPE);
        switch (type) {
            case EmailUserAction.DELETE: {
                final String currentEmail = args.getString(KEY_EMAIL_ADDRESS_CURRENT, "");
                final String currentPassword = args.getString(KEY_PASSWORD_CURRENT, "");
                final AuthCredential credential = EmailAuthProvider
                        .getCredential(currentEmail, currentPassword);
                return userRepo.deleteUser(firebaseUser, credential).toObservable();
            }
            case EmailUserAction.REQUEST_RESET_PW: {
                final String email = args.getString(KEY_EMAIL_ADDRESS_CURRENT, "");
                return userRepo.requestPasswordReset(email).toObservable();
            }
            case EmailUserAction.CHANGE_EMAIL_PW: {
                final String currentEmail = args.getString(KEY_EMAIL_ADDRESS_CURRENT, "");
                final String currentPassword = args.getString(KEY_PASSWORD_CURRENT, "");
                final String newEmail = args.getString(KEY_EMAIL_ADDRESS_NEW, "");
                final String newPassword = args.getString(KEY_PASSWORD_NEW, "");
                final AuthCredential credential = EmailAuthProvider
                        .getCredential(currentEmail, currentPassword);
                return userRepo.updateEmailPassword(firebaseUser, credential,
                        newEmail, newPassword).toObservable();
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
        activity.setEmailUserStream(observable.toSingle(), WORKER_TAG);
    }

    @IntDef({EmailUserAction.DELETE, EmailUserAction.REQUEST_RESET_PW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EmailUserAction {
        int DELETE = 1;
        int REQUEST_RESET_PW = 2;
        int CHANGE_EMAIL_PW = 3;
    }
}
