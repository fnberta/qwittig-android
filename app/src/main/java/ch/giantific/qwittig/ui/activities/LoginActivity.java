package ch.giantific.qwittig.ui.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.parse.ParseException;
import com.parse.ParseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.helpers.LoginHelper;
import ch.giantific.qwittig.ui.fragments.LoginBaseFragment;
import ch.giantific.qwittig.ui.fragments.LoginFragment;
import ch.giantific.qwittig.ui.fragments.LoginSignUpFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.ResetPasswordDialogFragment;


public class LoginActivity extends AppCompatActivity implements
        ResetPasswordDialogFragment.FragmentInteractionListener,
        LoginHelper.HelperInteractionListener {

    public static final String INTENT_URI_EMAIL = "intent_uri_email";
    public static final String INTENT_EXTRA_SIGN_UP = "intent_sign_up";
    public static final String LOGIN_FRAGMENT = "login_fragment";
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private LoginBaseFragment mLoginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        boolean showSignUpFragment = intent.getBooleanExtra(INTENT_EXTRA_SIGN_UP, false);
        String email = "";
        if (intent.hasExtra(INTENT_URI_EMAIL)) {
            email = intent.getStringExtra(INTENT_URI_EMAIL);
        }

        if (savedInstanceState == null) {
            Fragment fragment = showSignUpFragment ?
                    LoginSignUpFragment.newInstance(email) :
                    LoginFragment.newInstance(email);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, LOGIN_FRAGMENT)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mLoginFragment = (LoginBaseFragment) getFragmentManager().findFragmentByTag(LOGIN_FRAGMENT);
    }

    @Override
    public void onLoginFailed(ParseException e) {
        mLoginFragment.onLoginFailed(e);
    }

    @Override
    public void onLoginSucceeded(ParseUser parseUser) {
        mLoginFragment.onLoginSucceeded(parseUser);
    }

    @Override
    public void resetPassword(String email) {
        ((LoginFragment) mLoginFragment).resetPasswordWithHelper(email);
    }

    @Override
    public void onPasswordReset() {
        ((LoginFragment) mLoginFragment).onPasswordReset();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
