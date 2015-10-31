/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseConfig;
import com.parse.ParseException;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Config;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.data.helpers.account.LoginHelper;
import ch.giantific.qwittig.ui.activities.LoginActivity;
import ch.giantific.qwittig.ui.fragments.dialogs.ResetPasswordDialogFragment;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link LoginBaseFragment}.
 */
public class LoginFragment extends LoginBaseFragment {

    private TextInputLayout mTextInputLayoutEmail;
    @Nullable
    private EditText mEditTextPassword;
    private TextInputLayout mTextInputLayoutPassword;
    private Button mButtonLogIn;
    @Nullable
    private String mEmailInvited;

    public LoginFragment() {
    }

    /**
     * Returns a new instance of a {@link LoginFragment} with an email address as an argument.
     *
     * @param email the email to be used as an argument
     * @return a new instance of a {@link LoginFragment}
     */
    @NonNull
    public static LoginFragment newInstance(@NonNull String email) {
        LoginFragment fragment = new LoginFragment();

        Bundle args = new Bundle();
        args.putString(LoginActivity.INTENT_URI_EMAIL, email);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mEmailInvited = args.getString(LoginActivity.INTENT_URI_EMAIL);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.til_login_email);
        mEditTextEmail = (AutoCompleteTextView) mTextInputLayoutEmail.getEditText();
        mEditTextEmail.setText(mEmailInvited);

        mTextInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.til_login_password);
        mEditTextPassword = mTextInputLayoutPassword.getEditText();

        mButtonLogIn = (Button) view.findViewById(R.id.bt_login_login);
        mButtonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInUser();
            }
        });

        Button buttonSignUp = (Button) view.findViewById(R.id.bt_login_signup);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignUp();
            }
        });

        Button buttonTryOut = (Button) view.findViewById(R.id.bt_login_tryout);
        buttonTryOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryWithoutAccount();
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
            logInUserToParseWithHelper(email, password);
        } else {
            focusView.requestFocus();
        }
    }

    private void logInUserToParseWithHelper(@NonNull final String email, @NonNull String password) {
        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(mButtonLogIn, getString(R.string.toast_no_connection));
            return;
        }

        setLoading(true);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginHelper = HelperUtils.findHelper(fragmentManager, LOGIN_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginHelper == null) {
            loginHelper = LoginHelper.newInstanceLogin(email, password);

            fragmentManager.beginTransaction()
                    .add(loginHelper, LOGIN_HELPER)
                    .commit();
        }
    }

    private void launchSignUp() {
        String email = mEditTextEmail.getText().toString();

        FragmentManager fragmentManager = getFragmentManager();
        LoginSignUpFragment loginSignUpFragment = LoginSignUpFragment.newInstance(email);
        if (Utils.isRunningLollipopAndHigher()) {
            setFragmentTransitions(loginSignUpFragment);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, loginSignUpFragment)
                .addToBackStack(null)
                .commit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setFragmentTransitions(@NonNull LoginSignUpFragment loginSignUpFragment) {
        loginSignUpFragment.setEnterTransition(new Slide(Gravity.BOTTOM));

        setExitTransition(new Slide(Gravity.BOTTOM));
    }

    private void tryWithoutAccount() {
        ParseConfig config = ParseConfig.getCurrentConfig();
        String testUsersPassword = config.getString(Config.TEST_USERS_PASSWORD);
        List<String> testUsersNicknames = config.getList(Config.TEST_USERS_NICKNAMES);
        int testUserNumber = Utils.getRandomInt(testUsersNicknames.size());

        if (!TextUtils.isEmpty(testUsersPassword)) {
            logInUserToParseWithHelper(User.USERNAME_PREFIX_TEST + testUserNumber, testUsersPassword);
        } else {
            ParseErrorHandler.handleParseError(getActivity(), ParseException.CONNECTION_FAILED);
        }
    }

    private void showResetPasswordDialog() {
        String email = mEditTextEmail.getText().toString();

        DialogFragment resetPasswordDialogFragment = ResetPasswordDialogFragment.newInstance(email);
        resetPasswordDialogFragment.show(getFragmentManager(), "reset_password");
    }

    /**
     * Initiates the resetting of a user's password.
     *
     * @param email the email of the user whose password should be reset
     */
    public void onResetPasswordSelected(@NonNull String email) {
        resetPasswordWithHelper(email);
    }

    /**
     * Starts a helper fragment that sends the user and email to reset his/her password.
     *
     * @param email the email of the user whose password should be reset
     */
    private void resetPasswordWithHelper(@NonNull String email) {
        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(mButtonLogIn, getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginHelper = HelperUtils.findHelper(fragmentManager, LOGIN_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginHelper == null) {
            loginHelper = LoginHelper.newInstanceResetPassword(email);
            fragmentManager.beginTransaction()
                    .add(loginHelper, LOGIN_HELPER)
                    .commit();
        }
    }

    /**
     * Handles the successful reset of a password. Removes the helper fragment and tells the user
     * he needs to click on the link he/she received by email in order to reset the password.
     */
    public void onPasswordReset() {
        HelperUtils.removeHelper(getFragmentManager(), LOGIN_HELPER);
        MessageUtils.showBasicSnackbar(mButtonLogIn, getString(R.string.toast_reset_password_link));
    }
}
