/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addusers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsAddUsersBinding;
import ch.giantific.qwittig.di.components.DaggerSettingsAddUsersComponent;
import ch.giantific.qwittig.di.modules.SettingsAddUsersViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;

/**
 * Displays the user invite screen, where the user can invite new users to the group and sees
 * everybody that is currently invited but has not yet accepted/declined the invitation.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class SettingsAddUsersFragment extends BaseRecyclerViewFragment<SettingsAddUsersViewModel, SettingsAddUsersFragment.ActivityListener>
        implements SettingsAddUsersViewModel.ViewListener {

    private ProgressDialog mProgressDialog;
    private Intent mShareLink;
    private FragmentSettingsAddUsersBinding mBinding;

    public SettingsAddUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerSettingsAddUsersComponent.builder()
                .settingsAddUsersViewModelModule(new SettingsAddUsersViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);

        mShareLink = new Intent(Intent.ACTION_SEND);
        mShareLink.setType("text/plain");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSettingsAddUsersBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.rvAddUsers;
    }

    @Override
    protected RecyclerView.Adapter getRecyclerAdapter() {
        return new AddUsersRecyclerAdapter(mViewModel);
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setAddUserViewModel(mViewModel);
    }

    @Override
    public void loadAddUserWorker(@NonNull String nickname, @NonNull String groupId,
                                  @NonNull String groupName) {
        AddUserWorker.attach(getFragmentManager(), nickname, groupId, groupName);
    }

    @Override
    public void loadLinkShareOptions(@NonNull String link) {
        mShareLink.putExtra(Intent.EXTRA_TEXT, link);
        startActivity(Intent.createChooser(mShareLink, getString(R.string.action_share)));
    }

    @Override
    public void toggleProgressDialog(boolean show) {
        if (show) {
            mProgressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.progress_add_user), true, false);
        } else {
            mProgressDialog.hide();
        }
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * <p/>
     * Extends {@link BaseFragment.ActivityListener}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {
        /**
         * Sets the view model to the activty.
         *
         * @param viewModel the view model to set
         */
        void setAddUserViewModel(@NonNull SettingsAddUsersViewModel viewModel);
    }
}
