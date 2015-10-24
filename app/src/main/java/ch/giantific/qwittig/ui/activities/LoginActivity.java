package ch.giantific.qwittig.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

    public static final String INTENT_URI_EMAIL = "INTENT_URI_EMAIL";
    public static final String INTENT_EXTRA_SIGN_UP = "INTENT_EXTRA_SIGN_UP";
    private static final String STATE_LOGIN_FRAGMENT = "STATE_LOGIN_FRAGMENT";
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private LoginBaseFragment mLoginBaseFragment;

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

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            mLoginBaseFragment = showSignUpFragment ?
                    LoginSignUpFragment.newInstance(email) :
                    LoginFragment.newInstance(email);

            fragmentManager.beginTransaction()
                    .add(R.id.container, mLoginBaseFragment)
                    .commit();
        } else {
            mLoginBaseFragment = (LoginBaseFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_LOGIN_FRAGMENT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getFragmentManager().putFragment(outState, STATE_LOGIN_FRAGMENT, mLoginBaseFragment);
    }

    @Override
    public void onLoginFailed(ParseException e) {
        mLoginBaseFragment.onLoginFailed(e);
    }

    @Override
    public void onLoggedIn(ParseUser parseUser) {
        mLoginBaseFragment.onLoggedIn(parseUser);
    }

    @Override
    public void onResetPasswordSelected(String email) {
        ((LoginFragment) mLoginBaseFragment).onResetPasswordSelected(email);
    }

    @Override
    public void onPasswordReset() {
        ((LoginFragment) mLoginBaseFragment).onPasswordReset();
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
