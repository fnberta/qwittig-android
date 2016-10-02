/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import ch.giantific.qwittig.R;

/**
 * Provides a dialog that asks the user if he really wants to discard the changes made on a screen.
 * <p/>
 * Subclass of {@link BaseDialogFragment}.
 */
public class DiscardChangesDialogFragment extends BaseDialogFragment<DiscardChangesDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = DiscardChangesDialogFragment.class.getCanonicalName();

    /**
     * Displays a new instance of a {@link DiscardChangesDialogFragment}.
     *
     * @param fm the fragment manager to use for the transaction
     */
    public static void display(@NonNull FragmentManager fm) {
        final DiscardChangesDialogFragment dialog = new DiscardChangesDialogFragment();
        dialog.show(fm, DIALOG_TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(),
                R.style.Theme_AppCompat_Light_Dialog_Alert_PrimaryAsAccent);
        dialogBuilder.setMessage(R.string.dialog_discard_changes_message)
                .setPositiveButton(R.string.dialog_negative_discard, (dialog, id) -> {
                    activity.onDiscardChangesSelected();
                    dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dismiss());
        return dialogBuilder.create();
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on the discard changes button.
         */
        void onDiscardChangesSelected();
    }
}
