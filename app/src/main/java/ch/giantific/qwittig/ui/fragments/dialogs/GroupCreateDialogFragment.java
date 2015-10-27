/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import ch.giantific.qwittig.R;

/**
 * Provides a dialog that tells the use he needs to create a group in order to perform the
 * requested action.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class GroupCreateDialogFragment extends DialogFragment {

    private static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";
    private DialogInteractionListener mListener;
    private int mMessage;

    /**
     * Returns a new instance of {@link GroupCreateDialogFragment} with the message set.
     *
     * @param message the message to show in the dialog
     * @return a new instance of {@link GroupCreateDialogFragment}
     */
    @NonNull
    public static GroupCreateDialogFragment newInstance(int message) {
        GroupCreateDialogFragment fragment = new GroupCreateDialogFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_MESSAGE, message);
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
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mMessage = args.getInt(BUNDLE_MESSAGE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(mMessage)
                .setPositiveButton(R.string.dialog_positive_create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onCreateGroupSelected();
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return dialogBuilder.create();
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Handles the click on the create new group button.
         */
        void onCreateGroupSelected();
    }
}
