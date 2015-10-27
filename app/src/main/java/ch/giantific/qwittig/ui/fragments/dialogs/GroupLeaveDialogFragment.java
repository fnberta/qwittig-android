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
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import ch.giantific.qwittig.R;

/**
 * Provides a dialog that asks the user if he really wants to leave the group.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class GroupLeaveDialogFragment extends DialogFragment {

    private static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";
    private DialogInteractionListener mListener;
    private String mMessage;

    /**
     * Returns a new instance of {@link GroupLeaveDialogFragment}.
     *
     * @param message the message to display in the dialog
     * @return a new instance of {@link GroupLeaveDialogFragment}
     */
    @NonNull
    public static GroupLeaveDialogFragment newInstance(String message) {
        GroupLeaveDialogFragment fragment = new GroupLeaveDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_MESSAGE, message);
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
            mMessage = getArguments().getString(BUNDLE_MESSAGE, "");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(mMessage)
                .setPositiveButton(R.string.dialog_positive_leave, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onLeaveGroupSelected();
                        dismiss();
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
         * Handles the click on the leave group button.
         */
        void onLeaveGroupSelected();
    }
}
