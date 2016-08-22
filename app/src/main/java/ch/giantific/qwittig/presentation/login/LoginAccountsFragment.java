/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;

import ch.giantific.qwittig.databinding.FragmentLoginAccountsBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.login.di.LoginComponent;

/**
 * Displays the login screen asking the user for the username and password.
 * <p>
 * Subclass of {@link BaseFragment}.
 */
public class LoginAccountsFragment extends BaseFragment<LoginComponent, LoginAccountsViewModel, LoginAccountsFragment.ActivityListener>
        implements LoginAccountsViewModel.ViewListener {

    private FragmentLoginAccountsBinding binding;
    private CallbackManager facebookCallbackManager;

    public LoginAccountsFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        facebookCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginAccountsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btLoginAccountsFacebook.setFragment(this);
        binding.btLoginAccountsFacebook.setReadPermissions("email", "public_profile");
        binding.btLoginAccountsFacebook.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                viewModel.onFacebookSignedIn(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                viewModel.onFacebookLoginFailed();
            }

            @Override
            public void onError(FacebookException exception) {
                viewModel.onFacebookLoginFailed();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.attachView(this);
        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull LoginComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return binding.btLoginAccountsGoogle;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void loadFacebookLoginWorker(@NonNull String idToken) {
        LoginWorker.attachFacebookLoginInstance(getFragmentManager(), idToken);
    }

    @Override
    public void loginWithGoogle() {
        activity.loginWithGoogle();
    }

    @Override
    public void loadGoogleLoginWorker(@Nullable String tokenId) {
        LoginWorker.attachGoogleLoginInstance(getFragmentManager(), tokenId);
    }

    @Override
    public void showEmailFragment(@NonNull String identityId) {
        activity.showEmailFragment(identityId);
    }

    @Override
    public void showProfileFragment(boolean withInvitation) {
        activity.showProfileFragment(withInvitation);
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
