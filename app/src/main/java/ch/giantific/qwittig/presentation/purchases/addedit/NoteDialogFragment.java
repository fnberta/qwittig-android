/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.DialogPurchaseNoteBinding;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.BaseDialogFragment;

/**
 * Provides a dialog that allows the user to enter his/her email address in order to request a
 * password reset.
 * <p/>
 * If no valid email address is entered, the dialog is only be dismissed by the cancel action.
 * Therefore overrides the default positive button onClickListener because the default behaviour is
 * to always call dismiss().
 * <p/>
 * Subclass of {@link BaseDialogFragment}.
 */
public class NoteDialogFragment extends BaseDialogFragment<NoteDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = NoteDialogFragment.class.getCanonicalName();
    private static final String KEY_NOTE = "NOTE";

    /**
     * Displays a new instance of {@link NoteDialogFragment}.
     *
     * @param fm   the fragment manager to use for the transaction
     * @param note the note of the user if he/she already entered one
     */
    public static void display(@NonNull FragmentManager fm, @NonNull String note) {
        final NoteDialogFragment dialog = new NoteDialogFragment();

        final Bundle args = new Bundle();
        args.putString(KEY_NOTE, note);
        dialog.setArguments(args);

        dialog.show(fm, DIALOG_TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        final DialogPurchaseNoteBinding binding =
                DialogPurchaseNoteBinding.inflate(activity.getLayoutInflater());

        final String note = getArguments().getString(KEY_NOTE, "");
        if (!TextUtils.isEmpty(note)) {
            binding.etDialogPurchaseNote.setText(note);
        }

        dialogBuilder.setTitle(R.string.header_note)
                .setView(binding.getRoot())
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    final String note1 = binding.etDialogPurchaseNote.getText().toString();
                    this.activity.onNoteSet(note1);
                });

        if (TextUtils.isEmpty(note)) {
            dialogBuilder.setNegativeButton(android.R.string.no, (dialogInterface, i) -> dismiss());
        } else {
            dialogBuilder
                    .setNeutralButton(android.R.string.no, (dialog, which) -> dismiss())
                    .setNegativeButton(R.string.action_delete, (dialogInterface, i) -> this.activity.onDeleteNote());
        }

        return dialogBuilder.create();
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Defines the click on the save note button.
         *
         * @param note the note to set in the purchase
         */
        void onNoteSet(@NonNull String note);

        /**
         * Defines the click on the delete note button.
         */
        void onDeleteNote();
    }
}
