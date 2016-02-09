/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.fragments.ConfirmationDialogFragment;
import ch.giantific.qwittig.presentation.settings.addgroup.SettingsAddGroupActivity;

/**
 * Hosts {@link SettingsFragment} containing the main settings options.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsActivity extends BaseActivity implements
        ConfirmationDialogFragment.DialogInteractionListener,
        AccountDeleteDialogFragment.DialogInteractionListener,
        LogoutWorker.WorkerInteractionListener {

    private static final String STATE_SETTINGS_FRAGMENT = "STATE_SETTINGS_FRAGMENT";
    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // set default Result to OK, if logout is triggered it will be set to LOGOUT in order to
        // finish HomeActivity as well
        setResult(RESULT_OK);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            mSettingsFragment = new SettingsFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, mSettingsFragment)
                    .commit();
        } else {
            mSettingsFragment = (SettingsFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_SETTINGS_FRAGMENT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, STATE_SETTINGS_FRAGMENT, mSettingsFragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Snackbar.make(mToolbar, getString(R.string.toast_profile_update),
                                Snackbar.LENGTH_LONG).show();
                        break;
                    case SettingsProfileFragment.RESULT_CHANGES_DISCARDED:
                        Snackbar.make(mToolbar, getString(R.string.toast_changes_discarded), Snackbar.LENGTH_LONG).show();
                        break;
                }
                break;
            case INTENT_REQUEST_SETTINGS_GROUP_NEW:
                if (resultCode == Activity.RESULT_OK) {
                    final String newGroupName = data.getStringExtra(
                            SettingsAddGroupActivity.RESULT_DATA_GROUP);
                    Snackbar.make(mToolbar, getString(R.string.toast_group_added, newGroupName),
                            Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onActionConfirmed() {
        mSettingsFragment.onLeaveGroupSelected();
    }

    @Override
    public void onLoggedOut() {
        mSettingsFragment.onLoggedOut();
    }

    @Override
    public void onLogoutFailed(@StringRes int errorMessage) {
        mSettingsFragment.onLogoutFailed(errorMessage);
    }

    @Override
    public void onDeleteAccountSelected() {
        mSettingsFragment.onDeleteAccountSelected();
    }
}
