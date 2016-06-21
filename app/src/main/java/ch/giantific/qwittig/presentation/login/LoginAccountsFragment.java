/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginAccountsBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.login.di.LoginComponent;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginAccountsFragment extends BaseFragment<LoginComponent, LoginAccountsViewModel, LoginAccountsFragment.ActivityListener>
        implements LoginAccountsViewModel.ViewListener {

    private FragmentLoginAccountsBinding mBinding;

    public LoginAccountsFragment() {
        // required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentLoginAccountsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull LoginComponent component) {
        component.inject(this);
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
        mActivity.showEmailFragment(identityId);
    }

    @Override
    public void showProfileFragment(boolean withInvitation) {
        mActivity.showProfileFragment(withInvitation);
    }

    /**
     * Defines the interaction with the hosting activity.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener<LoginComponent> {

        /**
         * Starts the login with Google process.
         */
        void loginWithGoogle();

        void showEmailFragment(@NonNull String identityId);

        void showProfileFragment(boolean withInvitation);
    }
}
