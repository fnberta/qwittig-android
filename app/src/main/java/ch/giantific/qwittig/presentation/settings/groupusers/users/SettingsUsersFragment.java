/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsUsersBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsGroupUsersComponent;

/**
 * Displays the user invite screen, where the user can invite new users to the group and sees
 * everybody that is currently invited but has not yet accepted/declined the invitation.
 */
public class SettingsUsersFragment extends BaseFragment<SettingsGroupUsersComponent, SettingsUsersContract.Presenter, BaseFragment.ActivityListener<SettingsGroupUsersComponent>>
        implements SettingsUsersContract.ViewListener {

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

        final SettingsUsersRecyclerAdapter adapter = setupRecyclerView();
        presenter.attachView(this);
        presenter.setListInteraction(adapter);
        binding.setPresenter(presenter);
        binding.setViewModel(presenter.getViewModel());

        shareLink = new Intent(Intent.ACTION_SEND);
        shareLink.setType("text/plain");
    }

    @Override
    protected void injectDependencies(@NonNull SettingsGroupUsersComponent component) {
        component.inject(this);
    }

    private SettingsUsersRecyclerAdapter setupRecyclerView() {
        binding.rvSettingsUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvSettingsUsers.setHasFixedSize(true);
        final SettingsUsersRecyclerAdapter adapter = new SettingsUsersRecyclerAdapter(presenter);
        binding.rvSettingsUsers.setAdapter(adapter);

        return adapter;
    }

    @Override
    protected View getSnackbarView() {
        return binding.rvSettingsUsers;
    }

    @Override
    public void startEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
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

    @Override
    public String getGoogleApiKey() {
        return getString(R.string.google_api_key);
    }
}
