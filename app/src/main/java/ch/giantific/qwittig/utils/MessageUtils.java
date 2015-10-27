/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Provides useful static utility methods related to displaying messages to the user.
 */
public class MessageUtils {

    private static final String LOG_TAG = MessageUtils.class.getSimpleName();

    private MessageUtils() {
        // class cannot be instantiated
    }

    /**
     * Shows a basic {@link Snackbar} to the user.
     *
     * @param view    the view in whose parent the snackbar should be encapsulated
     * @param message the message to display
     */
    public static void showBasicSnackbar(@NonNull View view, @NonNull String message) {
        Snackbar snackbar = getBasicSnackbar(view, message);
        snackbar.show();
    }

    /**
     * Shows a basic {@link Snackbar} to the user with the option to define the duration it is shown.
     *
     * @param view     the view in whose parent the snackbar should be encapsulated
     * @param message  the message to display
     * @param duration the duration to show the snackbar
     */
    public static void showBasicSnackbar(@NonNull View view, @NonNull String message, int duration) {
        Snackbar snackbar = getBasicSnackbar(view, message, duration);
        snackbar.show();
    }

    /**
     * Returns a basic {@link Snackbar} with a message and the default long duration.
     *
     * @param view    the view in whose parent the snackbar should be encapsulated
     * @param message the message to display
     * @return a basic {@link Snackbar}
     */
    public static Snackbar getBasicSnackbar(@NonNull View view, @NonNull String message) {
        return getBasicSnackbar(view, message, Snackbar.LENGTH_LONG);
    }

    /**
     * Returns a basic {@link Snackbar} with a message and a duration.
     *
     * @param view     the view in whose parent the snackbar should be encapsulated
     * @param message  the message to display
     * @param duration the duration to show the snackbar
     * @return a basic {@link Snackbar}
     */
    public static Snackbar getBasicSnackbar(@NonNull View view, @NonNull String message, int duration) {
        return Snackbar.make(view, message, duration);
    }

    /**
     * Shows a basic {@link Toast} to the user with the default long duration.
     *
     * @param context the context to use to send the toast
     * @param message the message to display
     * @return a basic toast
     */
    public static Toast showToast(@NonNull Context context, @NonNull String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();

        return toast;
    }

    /**
     * Returns a {@link ProgressDialog} with a message that is not cancelable via press of the
     * back button.
     *
     * @param context the context to use to create the dialog
     * @param message the message to show
     * @return a {@link ProgressDialog}
     */
    @NonNull
    public static ProgressDialog getProgressDialog(@NonNull Context context,
                                                   @NonNull CharSequence message) {
        return getProgressDialog(context, message, false);
    }

    /**
     * Returns a {@link ProgressDialog} with a message.
     *
     * @param context      the context to use to create the dialog
     * @param message      the message to show
     * @param isCancelable whether the dialog is cancelable via a press of the back button or not
     * @return a {@link ProgressDialog}
     */
    @NonNull
    public static ProgressDialog getProgressDialog(@NonNull Context context,
                                                   @NonNull CharSequence message,
                                                   boolean isCancelable) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(isCancelable);
        progressDialog.setMessage(message);
        progressDialog.isIndeterminate();

        return progressDialog;
    }
}
