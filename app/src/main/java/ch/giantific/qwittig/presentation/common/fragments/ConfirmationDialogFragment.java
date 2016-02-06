/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

/**
 * Provides a dialog that asks the user to confirm an action.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class ConfirmationDialogFragment extends BaseDialogFragment<ConfirmationDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = ConfirmationDialogFragment.class.getCanonicalName();
    private static final String KEY_MESSAGE = "MESSAGE";
    private static final String KEY_POS_ACTION = "POS_ACTION";
    private String mMessage;
    private int mPosAction;

    /**
     * Displays a new instance of {@link ConfirmationDialogFragment}.
     *
     * @param message   the message to display in the dialog
     * @param posAction the positive action to display in the dialog
     */
    public static void display(@NonNull FragmentManager fm, @NonNull String message,
                               @StringRes int posAction) {
        final ConfirmationDialogFragment dialog = new ConfirmationDialogFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);
        args.putInt(KEY_POS_ACTION, posAction);
        dialog.setArguments(args);

        dialog.show(fm, DIALOG_TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mMessage = args.getString(KEY_MESSAGE);
            mPosAction = args.getInt(KEY_POS_ACTION);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(mMessage)
                .setPositiveButton(mPosAction, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mActivity.onActionConfirmed();
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, null);
        return dialogBuilder.create();
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on confirm button.
         */
        void onActionConfirmed();
    }
}
