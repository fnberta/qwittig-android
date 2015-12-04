/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.workerfragments.account.LoginWorker;
import ch.giantific.qwittig.ui.fragments.LoginAccountsFragment;
import ch.giantific.qwittig.ui.fragments.LoginBaseFragment;
import ch.giantific.qwittig.ui.fragments.LoginEmailFragment;
import ch.giantific.qwittig.ui.fragments.LoginEmailSignUpFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.EmailPromptDialogFragment;
import ch.giantific.qwittig.utils.Utils;

/**
 * Hosts {@link LoginEmailFragment} and {@link LoginEmailSignUpFragment} that handle the user login and
 * account creation processes.
 * <p/>
 * Subclass of {@link AppCompatActivity}.
 */
public class LoginActivity extends BaseActivity implements
        EmailPromptDialogFragment.DialogInteractionListener,
        LoginWorker.WorkerInteractionListener,
        LoginAccountsFragment.FragmentInteractionListener,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String INTENT_URI_EMAIL = "INTENT_URI_EMAIL";
    public static final int FRAGMENT_ACCOUNTS = 1;
    public static final int FRAGMENT_LOGIN_EMAIL = 2;
    public static final int FRAGMENT_SIGN_UP = 3;
    public static final int RC_SIGN_IN = 9001;
    private static final String FRAGMENT_LOGIN = "FRAGMENT_LOGIN";
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private String mEmail;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Intent intent = getIntent();
        if (intent.hasExtra(INTENT_URI_EMAIL)) {
            mEmail = intent.getStringExtra(INTENT_URI_EMAIL);
        }

        final FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            LoginBaseFragment fragment = new LoginAccountsFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.container, fragment, FRAGMENT_LOGIN)
                    .commit();
        }

        setupGoogleLogin();
    }

    private void setupGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        findLoginFragment().onLoginFailed(connectionResult.getErrorCode());
    }

    private LoginBaseFragment findLoginFragment() {
        return (LoginBaseFragment) getFragmentManager().findFragmentByTag(FRAGMENT_LOGIN);
    }

    @Override
    public void changeFragment(int type) {
        if (Utils.isRunningLollipopAndHigher()) {
            findLoginFragment().setExitTransition(new Slide(Gravity.BOTTOM));
        }

        LoginBaseFragment fragment = null;
        switch (type) {
            case FRAGMENT_ACCOUNTS:
                fragment = new LoginAccountsFragment();
                break;
            case FRAGMENT_LOGIN_EMAIL:
                fragment = LoginEmailFragment.newInstance(mEmail);
                break;
            case FRAGMENT_SIGN_UP:
                fragment = LoginEmailSignUpFragment.newInstance(mEmail);
                break;
        }

        if (fragment != null) {
            if (Utils.isRunningLollipopAndHigher()) {
                fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            }

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, FRAGMENT_LOGIN)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void loginWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            ((LoginAccountsFragment) findLoginFragment()).handleGoogleSignInResult(result);
        }
    }

    @Override
    public void onLoginFailed(int errorCode) {
        findLoginFragment().onLoginFailed(errorCode);
    }

    @Override
    public void onLoggedIn(@NonNull ParseUser parseUser) {
        setResult(RESULT_OK);
        findLoginFragment().onLoggedIn(parseUser);
    }

    @Override
    public void onValidEmailEntered(@NonNull String email) {
        Fragment fragment = findLoginFragment();
        if (fragment instanceof LoginEmailFragment) {
            ((LoginEmailFragment) fragment).resetPasswordWithWorker(email);
        } else if (fragment instanceof LoginAccountsFragment) {
            ((LoginAccountsFragment) fragment).setEmail(email);
        }
    }

    @Override
    public void onNoEmailEntered() {
        ((LoginAccountsFragment) findLoginFragment()).onNoEmailEntered();
    }

    @Override
    public void onPasswordReset() {
        ((LoginEmailFragment) findLoginFragment()).onPasswordReset();
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
