/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
    private TextInputLayout mTextInputLayoutEmail;
    private EditText mEditTextEmail;

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
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_prompt_email, null);

        mTextInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.til_dialog_email);
        mEditTextEmail = mTextInputLayoutEmail.getEditText();
        if (!TextUtils.isEmpty(mEmail)) {
            mEditTextEmail.setText(mEmail);
        }

        dialogBuilder.setTitle(mTitle)
                .setMessage(mMessage)
                .setView(view)
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
                    mEmail = mEditTextEmail.getText().toString();
                    if (Utils.isEmailValid(mEmail)) {
                        mTextInputLayoutEmail.setErrorEnabled(false);
                        mActivity.onValidEmailEntered(mEmail);
                        dismiss();
                    } else {
                        mTextInputLayoutEmail.setError(getString(R.string.error_email));
                    }
                }
            });
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        mActivity.onNoEmailEntered();
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

        /**
         * Defines the action to take when user cancels the dialog by pressing cancel, back or
         * anywhere outside the dialog.
         */
        void onNoEmailEntered();
    }
}
