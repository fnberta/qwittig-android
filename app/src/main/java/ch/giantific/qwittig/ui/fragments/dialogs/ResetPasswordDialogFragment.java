/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.Utils;

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
public class ResetPasswordDialogFragment extends DialogFragment {

    private static final String BUNDLE_EMAIL = "BUNDLE_EMAIL";
    private DialogInteractionListener mListener;
    private String mEmail;
    private TextInputLayout mTextInputLayoutEmail;

    /**
     * Returns a new instance of {@link ResetPasswordDialogFragment}.
     *
     * @param email the email address of the user if he/she already entered it in the email field
     *              of the login screen
     * @return a new instance of {@link ResetPasswordDialogFragment}
     */
    @NonNull
    public static ResetPasswordDialogFragment newInstance(String email) {
        ResetPasswordDialogFragment fragment = new ResetPasswordDialogFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_EMAIL, email);
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
            mEmail = getArguments().getString(BUNDLE_EMAIL, "");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_reset_password, null);
        mTextInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.til_email);
        if (!TextUtils.isEmpty(mEmail)) {
            mTextInputLayoutEmail.getEditText().setText(mEmail);
        }

        dialogBuilder.setTitle(R.string.dialog_login_reset_password_title)
                .setMessage(R.string.dialog_login_reset_password_message)
                .setView(view)
                .setPositiveButton(R.string.dialog_positive_reset, null)
                .setNegativeButton(android.R.string.no, null);

        return dialogBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Override View onClickListener because we only want the dialog to close when a valid
        // email address was entered. Default behavior is to always call dismiss().
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEmail = mTextInputLayoutEmail.getEditText().getText().toString();
                    if (Utils.emailIsValid(mEmail)) {
                        mTextInputLayoutEmail.setErrorEnabled(false);
                        mListener.onResetPasswordSelected(mEmail);
                        dismiss();
                    } else {
                        mTextInputLayoutEmail.setError(getString(R.string.error_email));
                    }
                }
            });
        }
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Defines the click on the reset password button.
         *
         * @param email the email address to send the reset password link to
         */
        void onResetPasswordSelected(@NonNull String email);
    }
}
