/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.parse.ParseConfig;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.parse.Config;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.activities.LoginActivity;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.workerfragments.account.LoginWorker;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link LoginBaseFragment}.
 */
public class LoginAccountsFragment extends LoginBaseFragment implements
        View.OnClickListener {

    private FragmentInteractionListener mListener;

    public LoginAccountsFragment() {
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_accounts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btFacebook = (Button) view.findViewById(R.id.bt_login_accounts_facebook);
        btFacebook.setOnClickListener(this);

        SignInButton btGoogle = (SignInButton) view.findViewById(R.id.bt_login_accounts_google);
        btGoogle.setOnClickListener(this);

        Button btLoginEmail = (Button) view.findViewById(R.id.bt_login_accounts_email);
        btLoginEmail.setOnClickListener(this);

        Button btSignUp = (Button) view.findViewById(R.id.bt_login_accounts_sign_up);
        btSignUp.setOnClickListener(this);

        Button btTryOut = (Button) view.findViewById(R.id.bt_login_accounts_tryout);
        btTryOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_login_accounts_facebook:
                loginWithFacebookWithWorker();
                break;
            case R.id.bt_login_accounts_google:
                loginWithGoogle();
                break;
            case R.id.bt_login_accounts_email:
                mListener.changeFragment(LoginActivity.FRAGMENT_LOGIN_EMAIL);
                break;
            case R.id.bt_login_accounts_sign_up:
                mListener.changeFragment(LoginActivity.FRAGMENT_SIGN_UP);
                break;
            case R.id.bt_login_accounts_tryout:
                tryWithoutAccount();
                break;
        }
    }

    private void loginWithFacebookWithWorker() {
        if (!Utils.isConnected(getActivity())) {
            Snackbar.make(mViewMain, R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginWorker = WorkerUtils.findWorker(fragmentManager, LOGIN_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginWorker == null) {
            loginWorker = LoginWorker.newInstanceFacebookLogin();

            fragmentManager.beginTransaction()
                    .add(loginWorker, LOGIN_WORKER)
                    .commit();
        }
    }

    private void loginWithGoogle() {
        if (!Utils.isConnected(getActivity())) {
            Snackbar.make(mViewMain, R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        mListener.loginWithGoogle();
    }

    /**
     * Handles the result of the attempt to sign in with Google.
     *
     * @param result the result of the sign in attempt
     */
    public void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String idToken = acct.getIdToken();
            String displayName = acct.getDisplayName();
            Uri photoUrl = acct.getPhotoUrl();
            verifyGoogleLoginTokenWithWorker(idToken, displayName, photoUrl);
        } else {
            onLoginFailed(R.string.toast_login_failed_google);
        }
    }

    private void verifyGoogleLoginTokenWithWorker(@NonNull String idToken,
                                                  @NonNull String displayName,
                                                  @NonNull Uri photoUrl) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginWorker = WorkerUtils.findWorker(fragmentManager, LOGIN_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginWorker == null) {
            loginWorker = LoginWorker.newInstanceGoogleLogin(idToken, displayName, photoUrl);

            fragmentManager.beginTransaction()
                    .add(loginWorker, LOGIN_WORKER)
                    .commit();
        }
    }

    private void tryWithoutAccount() {
        ParseConfig config = ParseConfig.getCurrentConfig();
        String testUsersPassword = config.getString(Config.TEST_USERS_PASSWORD);
        List<String> testUsersNicknames = config.getList(Config.TEST_USERS_NICKNAMES);
        int testUserNumber = Utils.getRandomInt(testUsersNicknames.size());

        if (!TextUtils.isEmpty(testUsersPassword)) {
            loginWithEmailWithWorker(User.USERNAME_PREFIX_TEST + testUserNumber, testUsersPassword);
        }
    }

    /**
     * Finds the login worker fragment and passes the entered email address.
     *
     * @param email the email address to pass to the login worker fragment
     */
    public void setEmail(String email) {
        LoginWorker loginWorker = (LoginWorker) WorkerUtils.findWorker(getFragmentManager(), LOGIN_WORKER);
        loginWorker.onValidEmailSet(email);
    }

    /**
     * Finds the login worker fragment and tells it to delete the newly created facebook user as
     * no valid email address was set.
     */
    public void onNoEmailEntered() {
        LoginWorker loginWorker = (LoginWorker) WorkerUtils.findWorker(getFragmentManager(), LOGIN_WORKER);
        loginWorker.onNoEmailSet();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface FragmentInteractionListener {
        /**
         * Changes the fragment to the type specified.
         * @param type the fragment type to change to
         */
        void changeFragment(int type);

        /**
         * Starts the login with Google process.
         */
        void loginWithGoogle();
    }
}
