/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginEmailBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.EmailPromptDialogFragment;
import ch.giantific.qwittig.presentation.login.di.LoginComponent;
import ch.giantific.qwittig.utils.ViewUtils;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginEmailFragment extends BaseFragment<LoginComponent, LoginEmailViewModel, LoginEmailFragment.ActivityListener>
        implements LoginEmailViewModel.ViewListener {

    private static final String KEY_IDENTITY_ID = "IDENTITY_ID";
    private FragmentLoginEmailBinding mBinding;

    public LoginEmailFragment() {
        // required empty constructor
    }

    public static LoginEmailFragment newInstance(@NonNull String identityId) {
        final LoginEmailFragment fragment = new LoginEmailFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_IDENTITY_ID, identityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentLoginEmailBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String identityId = getArguments().getString(KEY_IDENTITY_ID, "");
        mViewModel.setIdentityId(identityId);
        mViewModel.attachView(this);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull LoginComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.btLoginEmailLogin;
    }

    @Override
    public void showProfileScreen(boolean withInvitation) {
        mActivity.showProfileFragment(withInvitation);
    }

    @Override
    public void loadEmailLoginWorker(@NonNull String email, @NonNull String password,
                                     @NonNull String identityId) {
        LoginWorker.attachEmailLoginInstance(getFragmentManager(), email, password, identityId);
    }

    @Override
    public void loadEmailSignUpWorker(@NonNull String email, @NonNull String password,
                                      @NonNull String identityId) {
        LoginWorker.attachEmailSignUpInstance(getFragmentManager(), email, password, identityId);
    }

    @Override
    public void loadResetPasswordWorker(@NonNull String email) {
        LoginWorker.attachResetPasswordInstance(getFragmentManager(), email);
    }

    @Override
    public void showResetPasswordDialog(@NonNull String email) {
        EmailPromptDialogFragment.display(getFragmentManager(),
                R.string.dialog_login_reset_password_title,
                R.string.dialog_login_reset_password_message,
                R.string.dialog_positive_reset,
                email);
    }

    @Override
    public void hideKeyboard() {
        ViewUtils.hideSoftKeyboard(getActivity());
    }

    /**
     * Defines the interaction with the hosting activity.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener<LoginComponent> {

        void showProfileFragment(boolean withInvitation);
    }
}
