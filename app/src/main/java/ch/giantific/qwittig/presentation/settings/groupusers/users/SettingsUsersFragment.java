/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsUsersBinding;
import ch.giantific.qwittig.presentation.common.adapters.BaseRecyclerAdapter;
import ch.giantific.qwittig.presentation.common.fragments.BaseRecyclerViewFragment;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsGroupUsersComponent;

/**
 * Displays the user invite screen, where the user can invite new users to the group and sees
 * everybody that is currently invited but has not yet accepted/declined the invitation.
 * <p/>
 * Subclass of {@link BaseRecyclerViewFragment}.
 */
public class SettingsUsersFragment extends BaseRecyclerViewFragment<SettingsGroupUsersComponent, SettingsUsersViewModel, BaseRecyclerViewFragment.ActivityListener<SettingsGroupUsersComponent>>
        implements SettingsUsersViewModel.ViewListener {

    private FragmentSettingsUsersBinding binding;
    private ProgressDialog progressDialog;
    private Intent shareLink;

    public SettingsUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.attachView(this);
        viewModel.setListInteraction(recyclerAdapter);
        binding.setViewModel(viewModel);

        shareLink = new Intent(Intent.ACTION_SEND);
        shareLink.setType("text/plain");
    }

    @Override
    protected void injectDependencies(@NonNull SettingsGroupUsersComponent component) {
        component.inject(this);
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return binding.rvSettingsUsers;
    }

    @Override
    protected BaseRecyclerAdapter getRecyclerAdapter() {
        return new SettingsUsersRecyclerAdapter(viewModel);
    }

    @Override
    public void showProgressDialog(@StringRes int message) {
        progressDialog = ProgressDialog.show(getActivity(), null, getString(message), true, false);
    }

    @Override
    public void hideProgressDialog() {
        progressDialog.dismiss();
    }

    @Override
    public void loadLinkShareOptions(@NonNull String link) {
        shareLink.putExtra(Intent.EXTRA_TEXT, link);
        startActivity(Intent.createChooser(shareLink, getString(R.string.action_share)));
    }

    @Override
    public void showChangeNicknameDialog(@NonNull String nickname, int position) {
        NicknamePromptDialogFragment.display(getFragmentManager(), nickname, position);
    }
}
