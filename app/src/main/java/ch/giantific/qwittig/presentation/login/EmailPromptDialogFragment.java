/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.Button;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.DialogPromptEmailBinding;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.BaseDialogFragment;
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
public class EmailPromptDialogFragment extends BaseDialogFragment<EmailPromptDialogFragment.DialogInteractionListener> {

    private static final String DIALOG_TAG = EmailPromptDialogFragment.class.getCanonicalName();
    private static final String KEY_EMAIL = "EMAIL";

    private String email;
    private DialogPromptEmailBinding binding;

    /**
     * Shows a new instance of {@link EmailPromptDialogFragment}.
     *
     * @param fm    the fragment manager to use for the transaction
     * @param email the email to show
     */
    public static void display(@NonNull FragmentManager fm,
                               @NonNull String email) {
        final DialogFragment dialog = new EmailPromptDialogFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_EMAIL, email);
        dialog.setArguments(args);

        dialog.show(fm, DIALOG_TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            email = args.getString(KEY_EMAIL, "");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        binding = DialogPromptEmailBinding.inflate(activity.getLayoutInflater());

        if (!TextUtils.isEmpty(email)) {
            binding.etDialogEmail.setText(email);
        }

        dialogBuilder.setTitle(R.string.dialog_login_reset_password_title)
                .setMessage(R.string.dialog_login_reset_password_message)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.dialog_positive_reset, null)
                .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel());

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
            positiveButton.setOnClickListener(v -> {
                email = binding.etDialogEmail.getText().toString();
                if (Utils.isEmailValid(email)) {
                    binding.tilDialogEmail.setErrorEnabled(false);
                    activity.onValidEmailEntered(email);
                    dismiss();
                } else {
                    binding.tilDialogEmail.setError(getString(R.string.error_email));
                }
            });
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
        void onValidEmailEntered(@NonNull String email);
    }
}
