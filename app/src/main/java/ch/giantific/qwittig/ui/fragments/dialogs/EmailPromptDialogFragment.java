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
import android.support.design.widget.TextInputLayout;
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
public class EmailPromptDialogFragment extends DialogFragment {

    private static final String BUNDLE_EMAIL = "BUNDLE_EMAIL";
    private static final String BUNDLE_TITLE = "BUNDLE_TITLE";
    private static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";
    private static final String BUNDLE_POS_ACTION = "BUNDLE_POS_ACTION";
    private DialogInteractionListener mListener;
    private int mTitle;
    private int mMessage;
    private int mPosAction;
    private String mEmail;
    private TextInputLayout mTextInputLayoutEmail;
    private EditText mEditTextEmail;

    /**
     * Returns a new instance of {@link EmailPromptDialogFragment}.
     *
     * @param email the email address of the user if he/she already entered it in the email field
     *              of the login screen
     * @return a new instance of {@link EmailPromptDialogFragment}
     */
    @NonNull
    public static EmailPromptDialogFragment newInstance(@StringRes int title,
                                                        @StringRes int message,
                                                        @StringRes int posAction,
                                                        @NonNull String email) {
        EmailPromptDialogFragment fragment = new EmailPromptDialogFragment();

        Bundle args = new Bundle();
        args.putInt(BUNDLE_TITLE, title);
        args.putInt(BUNDLE_MESSAGE, message);
        args.putInt(BUNDLE_POS_ACTION, posAction);
        args.putString(BUNDLE_EMAIL, email);
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    public static EmailPromptDialogFragment newInstance(@StringRes int title,
                                                        @StringRes int message,
                                                        @StringRes int posAction) {
        EmailPromptDialogFragment fragment = new EmailPromptDialogFragment();

        Bundle args = new Bundle();
        args.putInt(BUNDLE_TITLE, title);
        args.putInt(BUNDLE_MESSAGE, message);
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
            mTitle = args.getInt(BUNDLE_TITLE);
            mMessage = args.getInt(BUNDLE_MESSAGE);
            mPosAction = args.getInt(BUNDLE_POS_ACTION);
            mEmail = args.getString(BUNDLE_EMAIL, "");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_prompt_email, null);

        mTextInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.til_email);
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
                    if (Utils.emailIsValid(mEmail)) {
                        mTextInputLayoutEmail.setErrorEnabled(false);
                        mListener.onValidEmailEntered(mEmail);
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

        mListener.onNoEmailEntered();
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
