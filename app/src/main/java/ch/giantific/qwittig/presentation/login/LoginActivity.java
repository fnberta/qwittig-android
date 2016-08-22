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

import com.google.android.gms.appinvite.AppInvite;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.GroupRepository;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorkerListener;
import ch.giantific.qwittig.presentation.login.di.DaggerLoginComponent;
import ch.giantific.qwittig.presentation.login.di.LoginAccountsViewModelModule;
import ch.giantific.qwittig.presentation.login.di.LoginComponent;
import ch.giantific.qwittig.presentation.login.di.LoginEmailViewModelModule;
import ch.giantific.qwittig.presentation.login.di.LoginFirstGroupViewModelModule;
import ch.giantific.qwittig.presentation.login.di.LoginInvitationViewModelModule;
import ch.giantific.qwittig.presentation.login.di.LoginProfileViewModelModule;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;

/**
 * Hosts fragments that handle the user login and account creation processes.
 * <p>
 * Subclass of {@link BaseActivity}.
 */
public class LoginActivity extends BaseActivity<LoginComponent> implements
        LoginAccountsFragment.ActivityListener,
        LoginEmailFragment.ActivityListener,
        LoginInvitationFragment.ActivityListener,
        LoginProfileFragment.ActivityListener,
        EmailPromptDialogFragment.DialogInteractionListener,
        LoginWorkerListener, EmailUserWorkerListener,
        GoogleApiClientDelegate.GoogleLoginCallback, GoogleApiClientDelegate.GoogleInvitationCallback {

    private static final String FRAGMENT_LOGIN = "FRAGMENT_LOGIN";
    private static final String PREF_FIRST_RUN = "PREF_FIRST_RUN";

    @Inject
    Navigator navigator;
    @Inject
    GoogleApiClientDelegate googleApiDelegate;
    private LoginAccountsViewModel accountsViewModel;
    private LoginEmailViewModel emailViewModel;
    private LoginInvitationViewModel invitationViewModel;
    private LoginProfileViewModel profileViewModel;
    private LoginFirstGroupViewModel firstGroupViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            checkFirstRun();
            addAccountsFragment();
        }

        googleApiDelegate.createGoogleApiClient(AppInvite.API);
        googleApiDelegate.checkForInvitation();
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        component = DaggerLoginComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .googleApiClientDelegateModule(new GoogleApiClientDelegateModule(this, this, this))
                .loginAccountsViewModelModule(new LoginAccountsViewModelModule(savedInstanceState))
                .loginEmailViewModelModule(new LoginEmailViewModelModule(savedInstanceState))
                .loginInvitationViewModelModule(new LoginInvitationViewModelModule(savedInstanceState))
                .loginProfileViewModelModule(new LoginProfileViewModelModule(savedInstanceState))
                .loginFirstGroupViewModelModule(new LoginFirstGroupViewModelModule(savedInstanceState))
                .build();
        component.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{accountsViewModel, emailViewModel,
                invitationViewModel, profileViewModel, firstGroupViewModel});
    }

    private void checkFirstRun() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean isFirstRun = prefs.getBoolean(PREF_FIRST_RUN, true);
        if (isFirstRun) {
            navigator.startFirstRun();
            prefs.edit().putBoolean(PREF_FIRST_RUN, false).apply();
        }
    }

    private void addAccountsFragment() {
        accountsViewModel = component.getLoginAccountsViewModel();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new LoginAccountsFragment(), FRAGMENT_LOGIN)
                .commit();
    }

    private void showInvitationFragment(@NonNull String groupName,
                                        @NonNull String inviterNickname) {
        invitationViewModel = component.getLoginInvitationViewModel();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleApiDelegate.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_IMAGE_PICK:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri imageUri = data.getData();
                    AvatarUtils.saveImageLocal(this, imageUri, new AvatarUtils.AvatarLocalSaveListener() {
                        @Override
                        public void onAvatarSaved(@NonNull String path) {
                            profileViewModel.onNewAvatarTaken(path);
                        }
                    });
                }
        }
    }

    @Override
    public void onDeepLinkFound(@NonNull Uri deepLink) {
        final String identityId = deepLink.getQueryParameter(GroupRepository.INVITATION_IDENTITY);
        final String groupName = deepLink.getQueryParameter(GroupRepository.INVITATION_GROUP);
        final String inviterNickname = deepLink.getQueryParameter(GroupRepository.INVITATION_INVITER);
        accountsViewModel.setInvitationIdentityId(identityId);
        showInvitationFragment(groupName, inviterNickname);
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        accountsViewModel.onGoogleLoginSuccessful(idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        accountsViewModel.onGoogleLoginFailed();
    }

    @Override
    public void loginWithGoogle() {
        googleApiDelegate.loginWithGoogle();
    }

    @Override
    public void showEmailFragment(@NonNull String identityId) {
        emailViewModel = component.getLoginEmailViewModel();
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
        profileViewModel = component.getLoginProfileViewModel();
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
        firstGroupViewModel = component.getLoginFirstGroupViewModel();
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
            accountsViewModel.setInvitationIdentityId("");
        }
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void setUserLoginStream(@NonNull Single<FirebaseUser> single, @NonNull String workerTag,
                                   @LoginWorker.LoginType int type) {
        switch (type) {
            case LoginWorker.LoginType.LOGIN_EMAIL:
                // fall through
            case LoginWorker.LoginType.SIGN_UP_EMAIL:
                emailViewModel.setUserLoginStream(single, workerTag, type);
                break;
            case LoginWorker.LoginType.LOGIN_FACEBOOK:
                // fall through
            case LoginWorker.LoginType.LOGIN_GOOGLE:
                accountsViewModel.setUserLoginStream(single, workerTag, type);
                break;
        }
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        emailViewModel.setEmailUserStream(single, workerTag);
    }

    @Override
    public void onValidEmailEntered(@NonNull String email) {
        emailViewModel.onValidEmailEntered(email);
    }
}
