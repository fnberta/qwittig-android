/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentLoginProfileBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.login.di.LoginComponent;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginProfileFragment extends BaseFragment<LoginComponent, LoginProfileViewModel, LoginProfileFragment.ActivityListener>
        implements LoginProfileViewModel.ViewListener {

    private static final String KEY_WITH_INVITATION = "WITH_INVITATION";

    private FragmentLoginProfileBinding binding;

    public LoginProfileFragment() {
        // required empty constructor
    }

    public static LoginProfileFragment newInstance(boolean withInvitation) {
        final LoginProfileFragment fragment = new LoginProfileFragment();
        final Bundle args = new Bundle();
        args.putBoolean(KEY_WITH_INVITATION, withInvitation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final boolean withInvitation = getArguments().getBoolean(KEY_WITH_INVITATION, false);
        viewModel.setWithInvitation(withInvitation);
        viewModel.attachView(this);
        binding.setViewModel(viewModel);
    }

    @Override
    protected void injectDependencies(@NonNull LoginComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return binding.ivLoginProfileAvatar;
    }

    @Override
    public void showFirstGroupScreen() {
        activity.showFirstGroupFragment();
    }

    /**
     * Defines the interaction with the hosting activity.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener<LoginComponent> {

        void showFirstGroupFragment();
    }
}
