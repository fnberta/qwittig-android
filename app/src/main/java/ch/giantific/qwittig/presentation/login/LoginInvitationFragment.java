/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginInvitationBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.login.di.DaggerLoginInvitationComponent;
import ch.giantific.qwittig.presentation.login.di.LoginInvitationViewModelModule;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginInvitationFragment extends BaseFragment<LoginInvitationViewModel, LoginInvitationFragment.ActivityListener>
        implements LoginInvitationViewModel.ViewListener {

    public static final String KEY_IDENTITY_ID = "IDENTITY_ID";
    public static final String KEY_GROUP_NAME = "GROUP_NAME";
    public static final String KEY_INVITER_NICKNAME = "INVITER_NICKNAME";
    private FragmentLoginInvitationBinding mBinding;

    public LoginInvitationFragment() {
        // required empty constructor
    }

    public static LoginInvitationFragment newInstance(@NonNull String identityId, @NonNull String groupName,
                                                      @NonNull String inviterNickname) {
        final LoginInvitationFragment fragment = new LoginInvitationFragment();
        final Bundle args = new Bundle();
        args.putString(KEY_IDENTITY_ID, identityId);
        args.putString(KEY_GROUP_NAME, groupName);
        args.putString(KEY_INVITER_NICKNAME, inviterNickname);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        final String groupName = args.getString(KEY_GROUP_NAME, "");
        final String inviterNickname = args.getString(KEY_INVITER_NICKNAME, "");

        DaggerLoginInvitationComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .loginInvitationViewModelModule(new LoginInvitationViewModelModule(savedInstanceState, this, groupName, inviterNickname))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentLoginInvitationBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected void setViewModelToActivity() {
        // nothing to set
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
     * Defines the interaction with the hosting {@link Activity}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {
        void popBackStack(boolean accepted);
    }
}
