/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.purchases.addedit;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.dialogs.BaseDialogFragment;

/**
 * Provides a dialog that asks the user if he wants to discard a purchase or save it as a draft.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class DiscardPurchaseDialogFragment extends BaseDialogFragment<DiscardPurchaseDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = DiscardPurchaseDialogFragment.class.getCanonicalName();

    /**
     * Displays a new instance of a {@link DiscardPurchaseDialogFragment}.
     *
     * @param fm the fragment manager to use for the transaction
     */
    public static void display(@NonNull FragmentManager fm) {
        final DiscardPurchaseDialogFragment dialog = new DiscardPurchaseDialogFragment();
        dialog.show(fm, DIALOG_TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(R.string.dialog_purchase_discard_message)
                .setPositiveButton(R.string.dialog_positive_save_draft, (dialog, id) -> {
                    activity.onSaveAsDraftSelected();
                    dismiss();
                })
                .setNegativeButton(R.string.dialog_purchase_discard, (dialog, which) -> activity.onDiscardPurchaseSelected());
        return dialogBuilder.create();
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on the save purchase as draft button.
         */
        void onSaveAsDraftSelected();

        /**
         * Handles the click on the discard purchase button.
         */
        void onDiscardPurchaseSelected();
    }
}
