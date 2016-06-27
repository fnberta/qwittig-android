/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.DialogPromptEmailBinding;
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
    private static final String KEY_TITLE = "TITLE";
    private static final String KEY_MESSAGE = "MESSAGE";
    private static final String KEY_POS_ACTION = "POS_ACTION";
    private int mTitle;
    private int mMessage;
    private int mPosAction;
    private String mEmail;
    private DialogPromptEmailBinding mBinding;

    /**
     * Shows a new instance of {@link EmailPromptDialogFragment}.
     *
     * @param fm        the fragment manager to use for the transaction
     * @param title     the title to show
     * @param message   the message to show
     * @param posAction the positive action to show
     * @param email     the email to show
     */
    public static void display(@NonNull FragmentManager fm, @StringRes int title,
                               @StringRes int message,
                               @StringRes int posAction,
                               @NonNull String email) {
        final DialogFragment dialog = new EmailPromptDialogFragment();
        final Bundle args = new Bundle();
        args.putInt(KEY_TITLE, title);
        args.putInt(KEY_MESSAGE, message);
        args.putInt(KEY_POS_ACTION, posAction);
        args.putString(KEY_EMAIL, email);
        dialog.setArguments(args);

        dialog.show(fm, DIALOG_TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getInt(KEY_TITLE);
            mMessage = args.getInt(KEY_MESSAGE);
            mPosAction = args.getInt(KEY_POS_ACTION);
            mEmail = args.getString(KEY_EMAIL, "");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        mBinding = DialogPromptEmailBinding.inflate(activity.getLayoutInflater());

        if (!TextUtils.isEmpty(mEmail)) {
            mBinding.etDialogEmail.setText(mEmail);
        }

        dialogBuilder.setTitle(mTitle)
                .setMessage(mMessage)
                .setView(mBinding.getRoot())
                .setPositiveButton(mPosAction, null)
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
                    mEmail = mBinding.etDialogEmail.getText().toString();
                    if (Utils.isEmailValid(mEmail)) {
                        mBinding.tilDialogEmail.setErrorEnabled(false);
                        mActivity.onValidEmailEntered(mEmail);
                        dismiss();
                    } else {
                        mBinding.tilDialogEmail.setError(getString(R.string.error_email));
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
         * Defines the action to take after the user entered a valid email address and hit enter.
         *
         * @param email the email address entered
         */
        void onValidEmailEntered(@NonNull String email);
    }
}