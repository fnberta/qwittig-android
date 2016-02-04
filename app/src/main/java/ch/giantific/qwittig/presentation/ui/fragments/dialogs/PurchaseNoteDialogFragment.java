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
import android.widget.EditText;

import ch.giantific.qwittig.R;

/**
 * Provides a dialog that allows the user to enter his/her email address in order to request a
 * password reset.
 * <p/>
 * If no valid email address is entered, the dialog is only be dismissed by the cancel action.
 * Therefore overrides the default positive button onClickListener because the default behaviour is
 * to always call dismiss().
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class PurchaseNoteDialogFragment extends DialogFragment {

    private static final String BUNDLE_NOTE = "BUNDLE_NOTE";
    private DialogInteractionListener mListener;
    private String mNote;
    private EditText mEditTextNote;

    /**
     * Returns a new instance of {@link PurchaseNoteDialogFragment}.
     *
     * @param note the note of the user if he/she already entered one
     * @return a new instance of {@link PurchaseNoteDialogFragment}
     */
    @NonNull
    public static PurchaseNoteDialogFragment newInstance(String note) {
        PurchaseNoteDialogFragment fragment = new PurchaseNoteDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_NOTE, note);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (DialogInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SeparateBillFragmentCallback");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mNote = getArguments().getString(BUNDLE_NOTE, "");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_purchase_note, null);
        TextInputLayout tilNote = (TextInputLayout) view.findViewById(R.id.til_note);
        mEditTextNote = tilNote.getEditText();
        if (mEditTextNote != null && !TextUtils.isEmpty(mNote)) {
            mEditTextNote.setText(mNote);
        }

        dialogBuilder.setTitle(R.string.header_note)
                .setView(view)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String note = mEditTextNote.getText().toString();
                        mListener.onNoteSet(note);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

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
    }
}
