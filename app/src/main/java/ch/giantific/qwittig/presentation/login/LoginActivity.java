/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseFacebookUtils;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.fragments.EmailPromptDialogFragment;
import rx.Single;

/**
 * Hosts fragments that handle the user login and account creation processes.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class LoginActivity extends BaseActivity<LoginAccountsViewModel> implements
        LoginAccountsFragment.ActivityListener,
        LoginEmailFragment.ActivityListener,
        EmailPromptDialogFragment.DialogInteractionListener,
        LoginWorkerListener {

    private static final String FRAGMENT_ACCOUNTS = "FRAGMENT_ACCOUNTS";
    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private LoginEmailViewModel mEmailViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            addAccountsFragment();
        }

        setupGoogleLogin();
    }

    private void addAccountsFragment() {
        final LoginAccountsFragment fragment = new LoginAccountsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment, FRAGMENT_ACCOUNTS)
                .commit();
    }

    private void setupGoogleLogin() {
        final GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        mViewModel.onGoogleLoginFailed();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleGoogleSignInResult(@NonNull GoogleSignInResult result) {
        if (result.isSuccess()) {
            final GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                final String idToken = acct.getIdToken();
                final String displayName = acct.getDisplayName();
                final Uri photoUrl = acct.getPhotoUrl();
                mViewModel.onGoogleSignedIn(idToken, displayName, photoUrl);
            } else {
                mViewModel.onGoogleLoginFailed();
            }
        } else {
            mViewModel.onGoogleLoginFailed();
        }
    }

    @Override
    public void setAccountsViewModel(@NonNull LoginAccountsViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void setEmailViewModel(@NonNull LoginEmailViewModel viewModel) {
        mEmailViewModel = viewModel;
    }

    @Override
    public void setUserLoginStream(@NonNull Single<User> single, @NonNull String workerTag,
                                   @LoginWorker.Type int type) {
        switch (type) {
            case LoginWorker.Type.LOGIN_EMAIL:
                // fall through
            case LoginWorker.Type.RESET_PASSWORD:
                // fall through
            case LoginWorker.Type.SIGN_UP_EMAIL:
                mEmailViewModel.setUserLoginStream(single, workerTag, type);
                break;
            case LoginWorker.Type.LOGIN_FACEBOOK:
                // fall through
            case LoginWorker.Type.LOGIN_GOOGLE:
                mViewModel.setUserLoginStream(single, workerTag, type);
                break;
        }
    }

    @Override
    public void loginWithGoogle() {
        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onValidEmailEntered(@NonNull String email) {
        mEmailViewModel.onValidEmailEntered(email);
    }

    @Override
    public void onNoEmailEntered() {
        mEmailViewModel.onNoEmailEntered();
    }
}
