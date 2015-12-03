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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.parse.ParseConfig;
import com.parse.ParseException;

import java.util.List;

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helpers.account.LoginHelper;
import ch.giantific.qwittig.domain.models.parse.Config;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.activities.LoginActivity;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.Utils;

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
                loginWithFacebookWithHelper();
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

    private void loginWithFacebookWithHelper() {
        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(mViewMain, getString(R.string.toast_no_connection));
            return;
        }

        setLoading(true);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginHelper = HelperUtils.findHelper(fragmentManager, LOGIN_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginHelper == null) {
            loginHelper = LoginHelper.newInstanceFacebookLogin();

            fragmentManager.beginTransaction()
                    .add(loginHelper, LOGIN_HELPER)
                    .commit();
        }
    }

    private void loginWithGoogle() {
        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(mViewMain, getString(R.string.toast_no_connection));
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
            verifyGoogleLoginTokenWithHelper(idToken, displayName, photoUrl);
        } else {
            // TODO: fix error code
            onLoginFailed(0);
        }
    }

    private void verifyGoogleLoginTokenWithHelper(@NonNull String idToken,
                                                  @NonNull String displayName,
                                                  @NonNull Uri photoUrl) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginHelper = HelperUtils.findHelper(fragmentManager, LOGIN_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginHelper == null) {
            loginHelper = LoginHelper.newInstanceGoogleLogin(idToken, displayName, photoUrl);

            fragmentManager.beginTransaction()
                    .add(loginHelper, LOGIN_HELPER)
                    .commit();
        }
    }

    private void tryWithoutAccount() {
        ParseConfig config = ParseConfig.getCurrentConfig();
        String testUsersPassword = config.getString(Config.TEST_USERS_PASSWORD);
        List<String> testUsersNicknames = config.getList(Config.TEST_USERS_NICKNAMES);
        int testUserNumber = Utils.getRandomInt(testUsersNicknames.size());

        if (!TextUtils.isEmpty(testUsersPassword)) {
            loginWithEmailWithHelper(User.USERNAME_PREFIX_TEST + testUserNumber, testUsersPassword);
        } else {
            ParseErrorHandler.handleParseError(getActivity(), ParseException.CONNECTION_FAILED);
        }
    }

    /**
     * Finds the login helper fragment and passes the entered email address.
     *
     * @param email the email address to pass to the login helper fragment
     */
    public void setEmail(String email) {
        LoginHelper loginHelper = (LoginHelper) HelperUtils.findHelper(getFragmentManager(), LOGIN_HELPER);
        loginHelper.onValidEmailSet(email);
    }

    /**
     * Finds the login helper fragment and tells it to delete the newly created facebook user as
     * no valid email address was set.
     */
    public void onNoEmailEntered() {
        LoginHelper loginHelper = (LoginHelper) HelperUtils.findHelper(getFragmentManager(), LOGIN_HELPER);
        loginHelper.onNoEmailSet();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void changeFragment(int type);

        void loginWithGoogle();
    }
}
