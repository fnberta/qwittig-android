/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.BaseDialogFragment;

/**
 * Provides a dialog that asks the user to confirm an action.
 * <p/>
 * Subclass of {@link BaseDialogFragment}.
 */
public class LeaveGroupDialogFragment extends BaseDialogFragment<LeaveGroupDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = LeaveGroupDialogFragment.class.getCanonicalName();
    private static final String KEY_MESSAGE = "MESSAGE";

    /**
     * Displays a new instance of {@link LeaveGroupDialogFragment}.
     *
     * @param message the message to display in the dialog
     */
    public static void display(@NonNull FragmentManager fm, int message) {
        final LeaveGroupDialogFragment dialog = new LeaveGroupDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(KEY_MESSAGE, message);
        dialog.setArguments(args);

        dialog.show(fm, DIALOG_TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int message = getArguments().getInt(KEY_MESSAGE);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(message)
                .setPositiveButton(R.string.dialog_positive_leave, (dialog, id) -> {
                    activity.onLeaveGroupSelected();
                    dismiss();
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
        void onLeaveGroupSelected();
    }
}
