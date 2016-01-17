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
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import ch.giantific.qwittig.R;

/**
 * Provides a dialog that allows the user to add a new store to the database.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class StoreAddDialogFragment extends DialogFragment {

    private DialogInteractionListener mListener;
    private TextInputLayout mTextInputLayoutStore;

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
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_store_add, null);
        mTextInputLayoutStore = (TextInputLayout) view.findViewById(R.id.til_store);

        dialogBuilder.setTitle(R.string.add_store)
                .setView(view)
                .setPositiveButton(R.string.dialog_positive_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String storeName = mTextInputLayoutStore.getEditText().getText().toString().trim();
                        if (!TextUtils.isEmpty(storeName)) {
                            mListener.onNewStoreSet(storeName);
                        }
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
         * Handles the click on the add new store button.
         *
         * @param storeName the new store to add
         */
        void onNewStoreSet(@NonNull String storeName);
    }
}
