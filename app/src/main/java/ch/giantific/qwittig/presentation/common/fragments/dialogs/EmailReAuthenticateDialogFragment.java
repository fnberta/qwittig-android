/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.fragments.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.DialogPromptEmailPasswordBinding;
import ch.giantific.qwittig.utils.Utils;

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
public class EmailReAuthenticateDialogFragment extends BaseDialogFragment<EmailReAuthenticateDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = EmailReAuthenticateDialogFragment.class.getCanonicalName();
    private static final String KEY_MESSAGE = "MESSAGE";
    private static final String KEY_EMAIL = "EMAIL";
    private int mMessageRes;
    private String mEmail;
    private DialogPromptEmailPasswordBinding mBinding;

    /**
     * Shows a new instance of {@link EmailReAuthenticateDialogFragment}.
     *
     * @param fm    the fragment manager to use for the transaction
     * @param email the email to show
     */
    public static void display(@NonNull FragmentManager fm,
                               @StringRes int messageRes,
                               @Nullable String email) {
        final DialogFragment dialog = new EmailReAuthenticateDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(KEY_MESSAGE, messageRes);
        args.putString(KEY_EMAIL, email);
        dialog.setArguments(args);

        dialog.show(fm, DIALOG_TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mMessageRes = args.getInt(KEY_MESSAGE);
            mEmail = args.getString(KEY_EMAIL, "");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        mBinding = DialogPromptEmailPasswordBinding.inflate(activity.getLayoutInflater());

        if (!TextUtils.isEmpty(mEmail)) {
            mBinding.etDialogEmail.setText(mEmail);
        }

        dialogBuilder.setMessage(mMessageRes)
                .setView(mBinding.getRoot())
                .setPositiveButton(R.string.dialog_positive_authenticate, null)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

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
                    validateInput();
                }
            });
        }
    }

    private void validateInput() {
        boolean valid = true;
        final String email = mBinding.etDialogEmail.getText().toString();
        if (!Utils.isEmailValid(email)) {
            mBinding.tilDialogEmail.setError(getString(R.string.error_email));
            valid = false;
        }

        final String password = mBinding.etDialogPassword.getText().toString();
        if (!Utils.isPasswordValid(password)) {
            mBinding.tilDialogPassword.setError(getString(R.string.error_password));
            valid = false;
        }

        if (valid) {
            mBinding.tilDialogEmail.setErrorEnabled(false);
            mBinding.tilDialogPassword.setErrorEnabled(false);
            mActivity.onValidEmailAndPasswordEntered(email, password);
            dismiss();
        }
    }

    /**
     * Defines the actions to take when user clicks on one of the dialog's buttons.
     */
    public interface DialogInteractionListener {
        /**
         * Defines the action to take after the user entered a valid email address and hit enter.
         *
         * @param email the email address entered
         */
        void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password);
    }
}
