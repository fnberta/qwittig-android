/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivitySettingsProfileBinding;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.settings.profile.UnlinkThirdPartyWorker.ProfileAction;
import rx.Single;

/**
 * Hosts {@link SettingsProfileFragment} that allows to user to change his profile information.
 * <p/>
 * Shows the user's avatar as backdrop image in the toolbar with a parallax collapse animation on
 * scroll.
 * <p/>
 * Subclass of {@link BaseActivity}.
 * <p/>
 *
 * @see android.support.design.widget.CollapsingToolbarLayout
 */
public class SettingsProfileActivity extends BaseActivity<SettingsProfileViewModel> implements
        SettingsProfileFragment.ActivityListener,
        DiscardChangesDialogFragment.DialogInteractionListener,
        UnlinkThirdPartyWorkerListener {

    private ActivitySettingsProfileBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings_profile);

        supportPostponeEnterTransition();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsProfileFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mViewModel.onExitClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        mViewModel.onExitClick();
    }

    @Override
    public void setProfileViewModel(@NonNull SettingsProfileViewModel viewModel) {
        mViewModel = viewModel;
        mBinding.setViewModel(mViewModel);
    }

    @Override
    public void setUnlinkActionStream(@NonNull Single<User> single, @NonNull String workerTag,
                                      @ProfileAction int action) {
        mViewModel.setUnlinkActionStream(single, workerTag, action);
    }

    @Override
    public void onDiscardChangesSelected() {
        mViewModel.onDiscardChangesSelected();
    }
}
