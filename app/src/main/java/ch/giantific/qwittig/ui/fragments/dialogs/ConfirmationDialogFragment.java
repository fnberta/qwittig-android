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
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

/**
 * Provides a dialog that asks the user to confirm an action.
 * <p/>
 * Subclass of {@link DialogFragment}.
 */
public class ConfirmationDialogFragment extends DialogFragment {

    private static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";
    private static final String BUNDLE_POS_ACTION = "BUNDLE_POS_ACTION";
    private DialogInteractionListener mListener;
    private String mMessage;
    private int mPosAction;

    /**
     * Returns a new instance of {@link ConfirmationDialogFragment}.
     *
     * @param message   the message to display in the dialog
     * @param posAction the positive action to display in the dialog
     * @return a new instance of {@link ConfirmationDialogFragment}
     */
    @NonNull
    public static ConfirmationDialogFragment newInstance(@NonNull String message,
                                                         @StringRes int posAction) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_MESSAGE, message);
        args.putInt(BUNDLE_POS_ACTION, posAction);
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

        Bundle args = getArguments();
        if (args != null) {
            mMessage = args.getString(BUNDLE_MESSAGE);
            mPosAction = args.getInt(BUNDLE_POS_ACTION);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(mMessage)
                .setPositiveButton(mPosAction, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onActionConfirmed();
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
         * Handles the click on confirm button.
         */
        void onActionConfirmed();
    }
}
