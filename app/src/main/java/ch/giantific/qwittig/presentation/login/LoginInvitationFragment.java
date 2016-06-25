/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.databinding.FragmentLoginInvitationBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.login.di.LoginComponent;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginInvitationFragment extends BaseFragment<LoginComponent, LoginInvitationViewModel, LoginInvitationFragment.ActivityListener>
        implements LoginInvitationViewModel.ViewListener {

    public static final String KEY_GROUP_NAME = "GROUP_NAME";
    public static final String KEY_INVITER_NICKNAME = "INVITER_NICKNAME";
    private FragmentLoginInvitationBinding mBinding;

    public LoginInvitationFragment() {
        // required empty constructor
    }

    public static LoginInvitationFragment newInstance(@NonNull String groupName,
                                                      @NonNull String inviterNickname) {
        final LoginInvitationFragment fragment = new LoginInvitationFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_GROUP_NAME, groupName);
        args.putString(KEY_INVITER_NICKNAME, inviterNickname);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentLoginInvitationBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Bundle args = getArguments();
        mViewModel.setGroupName(args.getString(KEY_GROUP_NAME, ""));
        mViewModel.setInviterNickname(args.getString(KEY_INVITER_NICKNAME, ""));
        mViewModel.attachView(this);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull LoginComponent component) {
        component.inject(this);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.tvLoginInvitationHeader;
    }

    @Override
    public void showAccountsScreen(boolean accept) {
        mActivity.popBackStack(accept);
    }

    /**
     * Defines the interaction with the hosting activity.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener<LoginComponent> {
        void popBackStack(boolean accepted);
    }
}
