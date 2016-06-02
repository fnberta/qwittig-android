/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginAccountsBinding;
import ch.giantific.qwittig.presentation.home.HomeActivity;
import ch.giantific.qwittig.presentation.login.di.DaggerLoginAccountsComponent;
import ch.giantific.qwittig.presentation.login.di.LoginAccountsViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.utils.Utils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import timber.log.Timber;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginAccountsFragment extends BaseFragment<LoginAccountsViewModel, LoginAccountsFragment.ActivityListener>
        implements LoginAccountsViewModel.ViewListener {

    private FragmentLoginAccountsBinding mBinding;

    public LoginAccountsFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerLoginAccountsComponent.builder()
                .loginAccountsViewModelModule(new LoginAccountsViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentLoginAccountsBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        checkBranchLink();
    }

    private void checkBranchLink() {
        final FragmentActivity activity = getActivity();
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

                    mViewModel.setInvitationIdentityId(identityId);
                    showInvitationFragment(identityId, groupName, inviterNickname);
                }
            }
        }, activity.getIntent().getData(), activity);
    }

    private void showInvitationFragment(@NonNull String identityId, @NonNull String groupName,
                                        @NonNull String inviterNickname) {
        final LoginInvitationFragment fragment =
                LoginInvitationFragment.newInstance(identityId, groupName, inviterNickname);
        if (Utils.isRunningLollipopAndHigher()) {
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, LoginActivity.FRAGMENT_LOGIN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setAccountsViewModel(mViewModel);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.btLoginAccountsGoogle;
    }

    @Override
    public void loadFacebookLoginWorker(@NonNull String identityId) {
        LoginWorker.attachFacebookLoginInstance(getFragmentManager(), identityId);
    }

    @Override
    public void loginWithGoogle() {
        mActivity.loginWithGoogle();
    }

    @Override
    public void loadGoogleTokenVerifyWorker(@Nullable String tokenId, @Nullable String displayName,
                                            @Nullable Uri photoUrl, @NonNull String identityId) {
        LoginWorker.attachGoogleVerifyTokenInstance(getFragmentManager(), tokenId, displayName,
                photoUrl, identityId);
    }

    @Override
    public void showEmailFragment(@NonNull String identityId) {
        final LoginEmailFragment fragment = LoginEmailFragment.newInstance(identityId);
        if (Utils.isRunningLollipopAndHigher()) {
            setExitTransition(new Slide(Gravity.BOTTOM));
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, LoginActivity.FRAGMENT_LOGIN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void finishScreen(int result) {
        final FragmentActivity activity = getActivity();
        activity.setResult(result);
        ActivityCompat.finishAfterTransition(activity);
    }

    @Override
    public void showProfileFragment() {
        final LoginProfileFragment fragment = new LoginProfileFragment();
        if (Utils.isRunningLollipopAndHigher()) {
            setExitTransition(new Slide(Gravity.START));
            fragment.setEnterTransition(new Slide(Gravity.END));
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, LoginActivity.FRAGMENT_LOGIN)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {

        /**
         * Sets the view model to the activity.
         *
         * @param viewModel the view model to set
         */
        void setAccountsViewModel(@NonNull LoginAccountsViewModel viewModel);

        /**
         * Starts the login with Google process.
         */
        void loginWithGoogle();
    }
}
