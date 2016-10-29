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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsUsersBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.BaseSortedListFragment;
import ch.giantific.qwittig.presentation.common.listadapters.BaseSortedListRecyclerAdapter;
import ch.giantific.qwittig.presentation.settings.groupusers.di.SettingsGroupUsersComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.SettingsUsersViewModel;
import ch.giantific.qwittig.presentation.settings.groupusers.users.viewmodels.items.SettingsUsersItemViewModel;

/**
 * Displays the user invite screen, where the user can invite new users to the group and sees
 * everybody that is currently invited but has not yet accepted/declined the invitation.
 */
public class SettingsUsersFragment extends BaseSortedListFragment<SettingsGroupUsersComponent,
        SettingsUsersContract.Presenter,
        BaseFragment.ActivityListener<SettingsGroupUsersComponent>,
        SettingsUsersItemViewModel>
        implements SettingsUsersContract.ViewListener {

    @Inject
    SettingsUsersViewModel viewModel;
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

        setupRecyclerView();
        presenter.attachView(this);
        binding.setPresenter(presenter);
        binding.setViewModel(viewModel);

        shareLink = new Intent(Intent.ACTION_SEND);
        shareLink.setType("text/plain");
    }

    @Override
    protected void injectDependencies(@NonNull SettingsGroupUsersComponent component) {
        component.inject(this);
    }

    @Override
    protected BaseSortedListRecyclerAdapter<SettingsUsersItemViewModel, SettingsUsersContract.Presenter, ? extends RecyclerView.ViewHolder> getRecyclerAdapter() {
        return new SettingsUsersRecyclerAdapter(presenter);
    }

    @Override
    protected void setupRecyclerView() {
        binding.rvSettingsUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvSettingsUsers.setHasFixedSize(true);
        binding.rvSettingsUsers.setAdapter(recyclerAdapter);
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
    public void loadInvitationLinkWorker(@NonNull String identityId,
                                         @NonNull String groupName,
                                         @NonNull String inviterNickname) {
        InvitationLinkWorker.attach(getFragmentManager(), identityId, groupName, inviterNickname);
    }
}
