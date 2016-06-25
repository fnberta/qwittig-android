/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.PreferenceManager;
import android.transition.Slide;
import android.view.Gravity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseFacebookUtils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.fragments.EmailPromptDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.login.di.DaggerLoginComponent;
import ch.giantific.qwittig.presentation.login.di.LoginAccountsViewModelModule;
import ch.giantific.qwittig.presentation.login.di.LoginComponent;
import ch.giantific.qwittig.presentation.login.di.LoginEmailViewModelModule;
import ch.giantific.qwittig.presentation.login.di.LoginFirstGroupViewModelModule;
import ch.giantific.qwittig.presentation.login.di.LoginInvitationViewModelModule;
import ch.giantific.qwittig.presentation.login.di.LoginProfileViewModelModule;
import ch.giantific.qwittig.presentation.purchases.list.HomeActivity;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.utils.Utils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import rx.Single;
import timber.log.Timber;

/**
 * Hosts fragments that handle the user login and account creation processes.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class LoginActivity extends BaseActivity<LoginComponent> implements
        LoginAccountsFragment.ActivityListener,
        LoginEmailFragment.ActivityListener,
        LoginInvitationFragment.ActivityListener,
        LoginProfileFragment.ActivityListener,
        EmailPromptDialogFragment.DialogInteractionListener,
        LoginWorkerListener {

    public static final String FRAGMENT_LOGIN = "FRAGMENT_LOGIN";
    private static final String GOOGLE_SERVER_ID = "1027430235430-ut0u3v7uh443akc3q6s3rhhvu3pfrsgi.apps.googleusercontent.com";
    private static final String PREF_FIRST_RUN = "PREF_FIRST_RUN";
    private static final int RC_SIGN_IN = 9001;
    LoginAccountsViewModel mAccountsViewModel;
    LoginEmailViewModel mEmailViewModel;
    LoginInvitationViewModel mInvitationViewModel;
    LoginProfileViewModel mProfileViewModel;
    LoginFirstGroupViewModel mFirstGroupViewModel;
    @Inject
    Navigator mNavigator;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            checkFirstRun();
            addAccountsFragment();
        }

        setupGoogleLogin();
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        mComponent = DaggerLoginComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .loginAccountsViewModelModule(new LoginAccountsViewModelModule(savedInstanceState))
                .loginEmailViewModelModule(new LoginEmailViewModelModule(savedInstanceState))
                .loginInvitationViewModelModule(new LoginInvitationViewModelModule(savedInstanceState))
                .loginProfileViewModelModule(new LoginProfileViewModelModule(savedInstanceState))
                .loginFirstGroupViewModelModule(new LoginFirstGroupViewModelModule(savedInstanceState))
                .build();
        mComponent.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mAccountsViewModel, mEmailViewModel,
                mInvitationViewModel, mProfileViewModel, mFirstGroupViewModel});
    }

    private void checkFirstRun() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean isFirstRun = prefs.getBoolean(PREF_FIRST_RUN, true);
        if (isFirstRun) {
            mNavigator.startFirstRun();
            prefs.edit().putBoolean(PREF_FIRST_RUN, false).apply();
        }
    }

    private void addAccountsFragment() {
        mAccountsViewModel = mComponent.getLoginAccountsViewModel();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new LoginAccountsFragment(), FRAGMENT_LOGIN)
                .commit();
    }

    private void setupGoogleLogin() {
        final GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(GOOGLE_SERVER_ID)
                        .requestEmail()
                        .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        mAccountsViewModel.onGoogleLoginFailed();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_SIGN_IN:
                final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleGoogleSignInResult(result);
                break;
            case Navigator.INTENT_REQUEST_IMAGE_PICK:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri imageUri = data.getData();
                    AvatarUtils.saveImageLocal(this, imageUri, new AvatarUtils.AvatarLocalSaveListener() {
                        @Override
                        public void onAvatarSaved(@NonNull String path) {
                            mProfileViewModel.onNewAvatarTaken(path);
                        }
                    });
                }
        }
    }

    private void handleGoogleSignInResult(@NonNull GoogleSignInResult result) {
        if (result.isSuccess()) {
            final GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                final String idToken = acct.getIdToken();
                final String displayName = acct.getDisplayName();
                final String email = acct.getEmail();
                final Uri photoUrl = acct.getPhotoUrl();
                mAccountsViewModel.onGoogleSignedIn(idToken, displayName, photoUrl);
            } else {
                mAccountsViewModel.onGoogleLoginFailed();
            }
        } else {
            mAccountsViewModel.onGoogleLoginFailed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkBranchLink();
    }

    private void checkBranchLink() {
        final Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error != null) {
                    Timber.e("deep link error, %s", error);
                    return;
                }

                final boolean openedWithInvite = referringParams.optBoolean(HomeActivity.BRANCH_IS_INVITE, false);
                if (openedWithInvite) {
                    final String identityId = referringParams.optString(HomeActivity.BRANCH_IDENTITY_ID);
                    final String groupName = referringParams.optString(HomeActivity.BRANCH_GROUP_NAME);
                    final String inviterNickname = referringParams.optString(HomeActivity.BRANCH_INVITER_NICKNAME);

                    mAccountsViewModel.setInvitationIdentityId(identityId);
                    showInvitationFragment(groupName, inviterNickname);
                }
            }
        }, getIntent().getData(), this);
    }

    private void showInvitationFragment(@NonNull String groupName,
                                        @NonNull String inviterNickname) {
        mInvitationViewModel = mComponent.getLoginInvitationViewModel();
        final Fragment fragment = LoginInvitationFragment.newInstance(groupName, inviterNickname);
        if (Utils.isRunningLollipopAndHigher()) {
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_LOGIN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void loginWithGoogle() {
        final Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void showEmailFragment(@NonNull String identityId) {
        mEmailViewModel = mComponent.getLoginEmailViewModel();
        final FragmentManager fm = getSupportFragmentManager();
        final LoginEmailFragment fragment = LoginEmailFragment.newInstance(identityId);
        if (Utils.isRunningLollipopAndHigher()) {
            final Fragment currentFrag = fm.findFragmentByTag(FRAGMENT_LOGIN);
            currentFrag.setExitTransition(new Slide(Gravity.BOTTOM));
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
        }

        fm.beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_LOGIN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showProfileFragment(boolean withInvitation) {
        mProfileViewModel = mComponent.getLoginProfileViewModel();
        final FragmentManager fm = getSupportFragmentManager();
        final LoginProfileFragment fragment = LoginProfileFragment.newInstance(withInvitation);
        if (Utils.isRunningLollipopAndHigher()) {
            final Fragment currentFrag = fm.findFragmentByTag(FRAGMENT_LOGIN);
            currentFrag.setExitTransition(new Slide(Gravity.START));
            currentFrag.setAllowReturnTransitionOverlap(false);
            fragment.setEnterTransition(new Slide(Gravity.END));
            fragment.setAllowEnterTransitionOverlap(false);
        }

        fm.beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_LOGIN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showFirstGroupFragment() {
        mFirstGroupViewModel = mComponent.getLoginFirstGroupViewModel();
        final FragmentManager fm = getSupportFragmentManager();
        final LoginFirstGroupFragment fragment = new LoginFirstGroupFragment();
        if (Utils.isRunningLollipopAndHigher()) {
            final Fragment currentFrag = fm.findFragmentByTag(FRAGMENT_LOGIN);
            currentFrag.setExitTransition(new Slide(Gravity.START));
            currentFrag.setAllowReturnTransitionOverlap(false);
            fragment.setEnterTransition(new Slide(Gravity.END));
            fragment.setAllowEnterTransitionOverlap(false);
        }

        fm.beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_LOGIN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void popBackStack(boolean accepted) {
        if (!accepted) {
            mAccountsViewModel.setInvitationIdentityId("");
        }
        getSupportFragmentManager().popBackStack();
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
                mAccountsViewModel.setUserLoginStream(single, workerTag, type);
                break;
        }
    }

    @Override
    public void onValidEmailEntered(@NonNull String email) {
        mEmailViewModel.onValidEmailEntered(email);
    }
}
