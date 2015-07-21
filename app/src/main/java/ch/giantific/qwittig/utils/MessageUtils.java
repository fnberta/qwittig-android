package ch.giantific.qwittig.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by fabio on 27.03.15.
 */
public class MessageUtils {

    private static final String LOG_TAG = MessageUtils.class.getSimpleName();

    private MessageUtils() {
        // class cannot be instantiated
    }

    public static void showBasicSnackbar(View view, String message) {
        Snackbar snackbar = getBasicSnackbar(view, message);
        snackbar.show();
    }

    public static Snackbar getBasicSnackbar(View view, String message) {
        return Snackbar.make(view, message, Snackbar.LENGTH_LONG);
    }

    public static Toast showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();

        return toast;
    }

    public static ProgressDialog getProgressDialog(Context context, CharSequence message) {
        return getProgressDialog(context, message, false);
    }

    public static ProgressDialog getProgressDialog(Context context, CharSequence message,
                                                   boolean isCancelable) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(isCancelable);
        progressDialog.setMessage(message);
        progressDialog.isIndeterminate();

        return progressDialog;
    }
}
