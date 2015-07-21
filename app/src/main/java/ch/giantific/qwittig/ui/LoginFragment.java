package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import ch.giantific.qwittig.ui.dialogs.ResetPasswordDialogFragment;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {

    private FragmentInteractionListener mListener;
    private AutoCompleteTextView mEditTextEmail;
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

    public static LoginFragment newInstance(String email) {
        LoginFragment fragment = new LoginFragment();

        Bundle args = new Bundle();
        args.putString(LoginActivity.INTENT_URI_EMAIL, email);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mEmailInvited = getArguments().getString(LoginActivity.INTENT_URI_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        mTextInputLayoutEmail = (TextInputLayout) rootView.findViewById(R.id.til_login_email);
        mEditTextEmail = (AutoCompleteTextView) mTextInputLayoutEmail.getEditText();
        mTextInputLayoutPassword = (TextInputLayout) rootView.findViewById(R.id.til_login_password);
        mEditTextPassword = mTextInputLayoutPassword.getEditText();
        mButtonLogIn = (Button) rootView.findViewById(R.id.bt_login_login);
        mButtonSignUp = (Button) rootView.findViewById(R.id.bt_login_signup);
        mButtonTryOut = (Button) rootView.findViewById(R.id.bt_login_tryout);
        mTextViewResetPassword = (TextView) rootView.findViewById(R.id.tv_reset_password);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditTextEmail.setText(mEmailInvited);
        mButtonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInUser();
            }
        });
        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignUp();
            }
        });
        mButtonTryOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryWithoutAccount();
            }
        });
        mTextViewResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetPasswordDialog();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListener.populateAutoComplete();
    }

    public void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEditTextEmail.setAdapter(adapter);
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
        }

        if (TextUtils.isEmpty(password)) {
            fieldsAreComplete = false;
            mTextInputLayoutPassword.setError(getString(R.string.error_login_password));
            focusView = mEditTextPassword;
        }

        if (fieldsAreComplete) {
            mListener.logInUser(email, password);
        } else {
            focusView.requestFocus();
        }
    }

    private void launchSignUp() {
        String email = mEditTextEmail.getText().toString();

        mListener.launchSignUpFragment(email);
    }

    /**
     * Logs user in with test account credentials.
     */
    private void tryWithoutAccount() {
        ParseConfig config = ParseConfig.getCurrentConfig();
        String testUsersPassword = config.getString(Config.TEST_USERS_PASSWORD);
        List<String> testUsersNicknames = config.getList(Config.TEST_USERS_NICKNAMES);
        int testUserNumber = Utils.getRandomInt(testUsersNicknames.size());

        if (!TextUtils.isEmpty(testUsersPassword)) {
            mListener.logInUser(User.USERNAME_PREFIX_TEST + testUserNumber, testUsersPassword);
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void logInUser(String username, String password);

        void launchSignUpFragment(String email);

        void populateAutoComplete();
    }
}
