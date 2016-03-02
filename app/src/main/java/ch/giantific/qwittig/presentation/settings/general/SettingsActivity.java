/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.fragments.LeaveGroupDialogFragment;
import ch.giantific.qwittig.presentation.settings.addgroup.SettingsAddGroupFragment;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileViewModel;
import rx.Single;

/**
 * Hosts {@link SettingsFragment} containing the main settings options.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsActivity extends BaseActivity<SettingsViewModel> implements
        SettingsFragment.ActivityListener,
        LeaveGroupDialogFragment.DialogInteractionListener,
        DeleteAccountDialogFragment.DialogInteractionListener,
        LogoutWorkerListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // set default Result to OK, if logout is triggered it will be set to LOGOUT in order to
        // finish HomeActivity as well
        setResult(RESULT_OK);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Snackbar.make(mToolbar, getString(R.string.toast_profile_update), Snackbar.LENGTH_LONG).show();
                        break;
                    case SettingsProfileViewModel.Result.CHANGES_DISCARDED:
                        Snackbar.make(mToolbar, getString(R.string.toast_changes_discarded), Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
            case INTENT_REQUEST_SETTINGS_ADD_GROUP:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final String newGroupName =
                                data.getStringExtra(SettingsAddGroupFragment.RESULT_DATA_GROUP);
                        mViewModel.onGroupAdded(newGroupName);
                        break;
                }
                break;
        }
    }

    @Override
    public void setSettingsViewModel(@NonNull SettingsViewModel viewModel) {
        mViewModel = viewModel;
    }

    @Override
    public void onLeaveGroupSelected() {
        mViewModel.onLeaveGroupSelected();
    }

    @Override
    public void setLogoutStream(@NonNull Single<User> single, @NonNull String workerTag) {
        mViewModel.setLogoutStream(single, workerTag);
    }

    @Override
    public void onDeleteAccountSelected() {
        mViewModel.onDeleteAccountSelected();
    }
}
