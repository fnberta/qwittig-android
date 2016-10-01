/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login.email;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentLoginEmailBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorker;
import ch.giantific.qwittig.presentation.login.LoginWorker;
import ch.giantific.qwittig.presentation.login.di.LoginComponent;
import ch.giantific.qwittig.utils.ViewUtils;

/**
 * Displays the login screen asking the user for the username and password.
 * <p>
 * Subclass of {@link BaseFragment}.
 */
public class LoginEmailFragment extends BaseFragment<LoginComponent, LoginEmailContract.Presenter, LoginEmailFragment.ActivityListener>
        implements LoginEmailContract.ViewListener {

    private static final String KEY_IDENTITY_ID = "IDENTITY_ID";
    private FragmentLoginEmailBinding binding;

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
        binding = FragmentLoginEmailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String identityId = getArguments().getString(KEY_IDENTITY_ID, "");
        presenter.setJoinIdentityId(identityId);
        presenter.attachView(this);
        binding.setPresenter(presenter);
        binding.setViewModel(presenter.getViewModel());
    }

    @Override
    protected void injectDependencies(@NonNull LoginComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return binding.btLoginEmailLogin;
    }

    @Override
    public void showProfileScreen(boolean withInvitation) {
        activity.showProfileFragment(withInvitation);
    }

    @Override
    public void loadEmailLoginWorker(@NonNull String email, @NonNull String password) {
        LoginWorker.attachEmailLoginInstance(getFragmentManager(), email, password);
    }

    @Override
    public void loadEmailSignUpWorker(@NonNull String email, @NonNull String password) {
        LoginWorker.attachEmailSignUpInstance(getFragmentManager(), email, password);
    }

    @Override
    public void loadResetPasswordWorker(@NonNull String email) {
        EmailUserWorker.attachResetPasswordInstance(getFragmentManager(), email);
    }

    @Override
    public void showResetPasswordDialog(@NonNull String email) {
        EmailPromptDialogFragment.display(getFragmentManager(), email);
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
