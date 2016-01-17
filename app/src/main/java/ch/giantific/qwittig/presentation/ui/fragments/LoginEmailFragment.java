/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.workerfragments.account.LoginWorker;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.EmailPromptDialogFragment;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link LoginBaseFragment}.
 */
public class LoginEmailFragment extends LoginEmailBaseFragment {

    private static final String RESET_PASSWORD_DIALOG = "RESET_PASSWORD_DIALOG";
    @Nullable
    private EditText mEditTextPassword;
    private TextInputLayout mTextInputLayoutPassword;
    private Button mButtonLogIn;

    public LoginEmailFragment() {
    }

    /**
     * Returns a new instance of a {@link LoginEmailFragment} with an email address as an argument.
     *
     * @param email the email to be used as an argument
     * @return a new instance of a {@link LoginEmailFragment}
     */
    @NonNull
    public static LoginEmailFragment newInstance(@NonNull String email) {
        LoginEmailFragment fragment = new LoginEmailFragment();

        Bundle args = new Bundle();
        args.putString(BUNDLE_EMAIL, email);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.til_login_password);
        mEditTextPassword = mTextInputLayoutPassword.getEditText();

        mButtonLogIn = (Button) view.findViewById(R.id.bt_login_email);
        mButtonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInUser();
            }
        });

        TextView textViewResetPassword = (TextView) view.findViewById(R.id.tv_reset_password);
        textViewResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetPasswordDialog();
            }
        });
    }

    private void logInUser() {
        View focusView = null;
        boolean fieldsAreComplete = true;

        String email = mEditTextEmail.getText().toString();
        String password = mEditTextPassword.getText().toString();

        if (!Utils.emailIsValid(email)) {
            fieldsAreComplete = false;
            mTextInputLayoutEmail.setError(getString(R.string.error_email));
            focusView = mEditTextEmail;
        } else {
            mTextInputLayoutEmail.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(password)) {
            fieldsAreComplete = false;
            mTextInputLayoutPassword.setError(getString(R.string.error_login_password));
            focusView = mEditTextPassword;
        } else {
            mTextInputLayoutPassword.setErrorEnabled(false);
        }

        if (fieldsAreComplete) {
            loginWithEmailWithWorker(email, password);
        } else {
            focusView.requestFocus();
        }
    }

    private void showResetPasswordDialog() {
        String email = mEditTextEmail.getText().toString();

        DialogFragment dialog = EmailPromptDialogFragment.newInstance(
                R.string.dialog_login_reset_password_title,
                R.string.dialog_login_reset_password_message,
                R.string.dialog_positive_reset,
                email);
        dialog.show(getFragmentManager(), RESET_PASSWORD_DIALOG);
    }

    /**
     * Starts a worker fragment that sends the user and email to reset his/her password.
     *
     * @param email the email of the user whose password should be reset
     */
    public void resetPasswordWithWorker(@NonNull String email) {
        if (!Utils.isNetworkAvailable(getActivity())) {
            Snackbar.make(mButtonLogIn, R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginWorker = WorkerUtils.findWorker(fragmentManager, LOGIN_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginWorker == null) {
            loginWorker = LoginWorker.newInstanceResetPassword(email);
            fragmentManager.beginTransaction()
                    .add(loginWorker, LOGIN_WORKER)
                    .commit();
        }
    }

    /**
     * Handles the successful reset of a password. Removes the worker fragment and tells the user
     * he needs to click on the link he/she received by email in order to reset the password.
     */
    public void onPasswordReset() {
        WorkerUtils.removeWorker(getFragmentManager(), LOGIN_WORKER);
        Snackbar.make(mButtonLogIn, R.string.toast_reset_password_link, Snackbar.LENGTH_LONG).show();
    }
}
