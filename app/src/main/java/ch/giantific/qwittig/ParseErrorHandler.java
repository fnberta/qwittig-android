/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig;


import android.content.Context;
import android.content.Intent;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import ch.giantific.qwittig.ui.activities.HomeActivity;

/**
 * Provides a generic error handler for exceptions thrown by the Parse.com framework.
 */
public class ParseErrorHandler {

    private static final String LOG_TAG = ParseErrorHandler.class.getSimpleName();

    private ParseErrorHandler() {
        // class cannot be instantiated
    }

    /**
     * Handles {@link ParseException} errors. Currently only handles
     * {@link ParseException#INVALID_SESSION_TOKEN}, for which it forces the user to log in again.
     *
     * @param context   the context to use
     * @param errorCode the error code to handle
     */
    public static void handleParseError(final Context context, int errorCode) {
        switch (errorCode) {
            case ParseException.INVALID_SESSION_TOKEN: {
                forceNewLogin(context.getApplicationContext());

                // TODO: can we unsubscribe from notification channels?
                break;
            }
        }
    }

    /**
     * Forces the user to the login screen because his/her session token is no longer valid and a
     * new login is required.
     *
     * @param context the context used to construct the intent
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

    /**
     * Returns an appropriate error message for different {@link ParseException}.
     *
     * @param context   the context to use to resolve the string resource
     * @param errorCode the error code for which to get the error message
     * @return an appropriate error message
     */
    public static String getErrorMessage(Context context, int errorCode) {
        String errorMessage;
        switch (errorCode) {
            case ParseException.USERNAME_TAKEN:
                errorMessage = context.getString(R.string.toast_email_address_taken);
                break;
            case ParseException.OBJECT_NOT_FOUND:
                errorMessage = context.getString(R.string.toast_login_failed_credentials);
                break;
            case ParseException.CONNECTION_FAILED:
                errorMessage = context.getString(R.string.toast_no_connection);
                break;
            default:
                errorMessage = context.getString(R.string.toast_unknown_error);
        }

        return errorMessage;
    }
}
