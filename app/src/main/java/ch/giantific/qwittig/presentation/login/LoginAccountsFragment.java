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

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginAccountsBinding;
import ch.giantific.qwittig.presentation.login.di.DaggerLoginAccountsComponent;
import ch.giantific.qwittig.presentation.login.di.LoginAccountsViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginAccountsFragment extends BaseFragment<LoginAccountsViewModel, LoginAccountsFragment.ActivityListener>
        implements LoginAccountsViewModel.ViewListener {

    private static final String FRAGMENT_LOGIN = "FRAGMENT_LOGIN";
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
    protected void setViewModelToActivity() {
        mActivity.setAccountsViewModel(mViewModel);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.btLoginAccountsGoogle;
    }

    @Override
    public void loadFacebookLoginWorker() {
        LoginWorker.attachFacebookLoginInstance(getFragmentManager());
    }

    @Override
    public void loginWithGoogle() {
        mActivity.loginWithGoogle();
    }

    @Override
    public void loadGoogleTokenVerifyWorker(@Nullable String tokenId, @Nullable String displayName,
                                            @Nullable Uri photoUrl) {
        LoginWorker.attachGoogleVerifyTokenInstance(getFragmentManager(), tokenId, displayName,
                photoUrl);
    }

    @Override
    public void showEmailFragment() {
        final LoginEmailFragment fragment = new LoginEmailFragment();
        if (Utils.isRunningLollipopAndHigher()) {
            setExitTransition(new Slide(Gravity.BOTTOM));
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, FRAGMENT_LOGIN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void finishScreen(int result) {
        final FragmentActivity activity = getActivity();
        activity.setResult(result);
        ActivityCompat.finishAfterTransition(activity);
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
