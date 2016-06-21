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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.fragments.LeaveGroupDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.settings.general.di.DaggerSettingsComponent;
import ch.giantific.qwittig.presentation.settings.general.di.SettingsComponent;
import ch.giantific.qwittig.presentation.settings.general.di.SettingsViewModelModule;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupFragment;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileViewModel;
import rx.Single;

/**
 * Hosts {@link SettingsFragment} containing the main settings options.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsActivity extends BaseActivity<SettingsComponent> implements
        LeaveGroupDialogFragment.DialogInteractionListener,
        DeleteAccountDialogFragment.DialogInteractionListener,
        LogoutWorkerListener {

    @Inject
    SettingsViewModel mSettingsViewModel;

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
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        mComponent = DaggerSettingsComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .settingsViewModelModule(new SettingsViewModelModule(savedInstanceState))
                .build();
        mComponent.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mSettingsViewModel});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Snackbar.make(mToolbar, getString(R.string.toast_profile_update), Snackbar.LENGTH_LONG).show();
                        break;
                    case SettingsProfileViewModel.Result.CHANGES_DISCARDED:
                        Snackbar.make(mToolbar, getString(R.string.toast_changes_discarded), Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
            case Navigator.INTENT_REQUEST_SETTINGS_ADD_GROUP:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final String newGroupName =
                                data.getStringExtra(SettingsAddGroupFragment.RESULT_DATA_GROUP);
                        mSettingsViewModel.onGroupAdded(newGroupName);
                        break;
                }
                break;
        }
    }

    @Override
    public void onLeaveGroupSelected() {
        mSettingsViewModel.onLeaveGroupSelected();
    }

    @Override
    public void setLogoutStream(@NonNull Single<User> single, @NonNull String workerTag) {
        mSettingsViewModel.setLogoutStream(single, workerTag);
    }

    @Override
    public void onDeleteAccountSelected() {
        mSettingsViewModel.onDeleteAccountSelected();
    }
}
