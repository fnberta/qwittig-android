/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import ch.giantific.qwittig.R;

/**
 * Provides a dialog that asks the user if he wants to discard a purchase or save it as a draft.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class PurchaseDiscardDialogFragment extends DialogFragment {

    private DialogInteractionListener mListener;

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(R.string.dialog_purchase_discard_message)
                .setPositiveButton(R.string.dialog_positive_save_draft, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onSavePurchaseAsDraftSelected();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_purchase_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDiscardPurchaseSelected();
                    }
                });
        return dialogBuilder.create();
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on the save purchase as draft button.
         */
        void onSavePurchaseAsDraftSelected();

        /**
         * Handles the click on the discard purchase button.
         */
        void onDiscardPurchaseSelected();
    }
}
