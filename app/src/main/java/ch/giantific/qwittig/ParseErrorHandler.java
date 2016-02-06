/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import ch.giantific.qwittig.presentation.home.purchases.list.HomeActivity;
import timber.log.Timber;

/**
 * Provides a generic error handler for exceptions thrown by the Parse.com framework.
 */
public class ParseErrorHandler {

    private ParseErrorHandler() {
        // class cannot be instantiated
    }

    /**
     * Returns the appropriate error message for the specified {@link ParseException}. If exception
     * is {@link ParseException#INVALID_SESSION_TOKEN}, forces the user to log in again.
     *
     * @param context the context to use
     * @param e       the exception to handle
     * @return the appropriate error message for the exception
     */
    @StringRes
    public static int handleParseError(@NonNull Context context, @NonNull ParseException e) {
        switch (e.getCode()) {
            case ParseException.INVALID_SESSION_TOKEN:
                forceNewLogin(context.getApplicationContext());
                return R.string.toast_invalid_session;
            case ParseException.USERNAME_TAKEN:
                return R.string.toast_email_address_taken;
            case ParseException.OBJECT_NOT_FOUND:
                return R.string.toast_login_failed_credentials;
            case ParseException.CONNECTION_FAILED:
                return R.string.toast_no_connection;
            default:
                Timber.e(e, "unknown error");
                return R.string.toast_unknown_error;
        }
    }

    /**
     * Forces the user to the login screen because his/her session token is no longer valid and a
     * new login is required.
     * TODO: can we un-subscribe from notification channels before logging out?
     *
     * @param context the context used to construct the intent, may be an application context as
     *                we start the activity as a new task
     */
    private static void forceNewLogin(final Context context) {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                // ignore possible exception, currentUser will always be null now
                Intent intent = new Intent(context, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }
}
