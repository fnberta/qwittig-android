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
 * Provides a dialog that asks the user if he really wants to delete his/her account.
 * <p/>
 * Subclass of {@link BaseDialogFragment}.
 */
public class DeleteAccountDialogFragment extends BaseDialogFragment<DeleteAccountDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = DeleteAccountDialogFragment.class.getCanonicalName();

    /**
     * Displays a new {@link DeleteAccountDialogFragment} instance.
     *
     * @param fm the fragment manager to use for the transaction
     */
    public static void display(@NonNull FragmentManager fm) {
        final DeleteAccountDialogFragment dialog = new DeleteAccountDialogFragment();
        dialog.show(fm, DIALOG_TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(R.string.dialog_account_delete_message)
                .setPositiveButton(R.string.dialog_positive_delete, (dialog, id) -> {
                    activity.onDeleteAccountSelected();
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
         * Handles the click on the yes, delete my account button.
         */
        void onDeleteAccountSelected();
    }
}
