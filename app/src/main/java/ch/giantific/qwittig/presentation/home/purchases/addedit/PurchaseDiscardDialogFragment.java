/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home.purchases.addedit;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.BaseDialogFragment;

/**
 * Provides a dialog that asks the user if he wants to discard a purchase or save it as a draft.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class PurchaseDiscardDialogFragment extends BaseDialogFragment<PurchaseDiscardDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = PurchaseDiscardDialogFragment.class.getCanonicalName();

    /**
     * Displays a new instance of a {@link PurchaseDiscardDialogFragment}.
     *
     * @param fm the fragment manager to use for the transaction
     */
    public static void display(@NonNull FragmentManager fm) {
        final PurchaseDiscardDialogFragment dialog = new PurchaseDiscardDialogFragment();
        dialog.show(fm, DIALOG_TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(R.string.dialog_purchase_discard_message)
                .setPositiveButton(R.string.dialog_positive_save_draft, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mActivity.onSavePurchaseAsDraftSelected();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_purchase_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActivity.onDiscardPurchaseSelected();
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
