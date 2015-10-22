package ch.giantific.qwittig.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
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
import ch.giantific.qwittig.data.parse.models.Config;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helpers.LoginHelper;
import ch.giantific.qwittig.ui.activities.LoginActivity;
import ch.giantific.qwittig.ui.fragments.dialogs.ResetPasswordDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends LoginBaseFragment {

    public static final String LOGIN_HELPER = "login_helper";
    private TextInputLayout mTextInputLayoutEmail;
    private EditText mEditTextPassword;
    private TextInputLayout mTextInputLayoutPassword;
    private Button mButtonLogIn;
    private Button mButtonSignUp;
    private Button mButtonTryOut;
    private TextView mTextViewResetPassword;
    private String mEmailInvited;

    public LoginFragment() {
    }

    /**
     * Returns a new instance of a {@link LoginFragment} with an email address as an argument.
     * @param email the email to be used as an argument
     * @return a new instance of a {@link LoginFragment}
     */
    public static LoginFragment newInstance(String email) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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

        mButtonSignUp = (Button) view.findViewById(R.id.bt_login_signup);
        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignUp();
            }
        });

        mButtonTryOut = (Button) view.findViewById(R.id.bt_login_tryout);
        mButtonTryOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryWithoutAccount();
            }
        });

        mTextViewResetPassword = (TextView) view.findViewById(R.id.tv_reset_password);
        mTextViewResetPassword.setOnClickListener(new View.OnClickListener() {
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

    private void logInUserToParseWithHelper(final String email, String password) {
        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(mButtonLogIn, getString(R.string.toast_no_connection));
            return;
        }

        setLoading(true);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginHelper = findHelper(fragmentManager, LOGIN_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginHelper == null) {
            loginHelper = LoginHelper.newInstance(email, password);

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
                .replace(R.id.container, loginSignUpFragment, LoginActivity.LOGIN_FRAGMENT)
                .addToBackStack(null)
                .commit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setFragmentTransitions(LoginSignUpFragment loginSignUpFragment) {
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
            ParseErrorHandler.handleParseError(getActivity(),
                    new ParseException(ParseException.CONNECTION_FAILED, ""));
        }
    }

    private void showResetPasswordDialog() {
        String email = mEditTextEmail.getText().toString();

        DialogFragment resetPasswordDialogFragment = ResetPasswordDialogFragment.newInstance(email);
        resetPasswordDialogFragment.show(getFragmentManager(), "reset_password");
    }

    /**
     * Starts a helper fragment that resets a user's password.
     * @param email the email of the user the whose password should be reset
     */
    public void resetPasswordWithHelper(String email) {
        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(mButtonLogIn, getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginHelper = findHelper(fragmentManager, LOGIN_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginHelper == null) {
            loginHelper = LoginHelper.newInstance(email);

            fragmentManager.beginTransaction()
                    .add(loginHelper, LOGIN_HELPER)
                    .commit();
        }
    }

    /**
     * Handles the successful reset of a password. Removes the helper fragment and tells the user
     * he needs to click on the link he received by email in order to really reset his password.
     */
    public void onPasswordReset() {
        removeHelper(LOGIN_HELPER);
        MessageUtils.showBasicSnackbar(mButtonLogIn, getString(R.string.toast_reset_password_link));
    }
}
