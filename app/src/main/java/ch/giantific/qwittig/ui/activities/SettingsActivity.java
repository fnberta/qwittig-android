/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.workerfragments.account.LogoutWorker;
import ch.giantific.qwittig.ui.fragments.SettingsFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.AccountDeleteDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.ConfirmationDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupLeaveBalanceNotZeroDialogFragment;

/**
 * Hosts {@link SettingsFragment} containing the main settings options.
 * <p/>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsActivity extends BaseActivity implements
        ConfirmationDialogFragment.DialogInteractionListener,
        SettingsFragment.FragmentInteractionListener,
        GroupLeaveBalanceNotZeroDialogFragment.DialogInteractionListener,
        AccountDeleteDialogFragment.DialogInteractionListener,
        LogoutWorker.WorkerInteractionListener {

    private static final String STATE_SETTINGS_FRAGMENT = "STATE_SETTINGS_FRAGMENT";
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // set default Result to OK, if logOut is triggered it will be set to LOGOUT in order to
        // finish HomeActivity as well
        setResult(RESULT_OK);

        FragmentManager fragmentManager = getFragmentManager();
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

        getFragmentManager().putFragment(outState, STATE_SETTINGS_FRAGMENT, mSettingsFragment);
    }

    @Override
    public void onActionConfirmed() {
        mSettingsFragment.onLeaveGroupSelected();
    }

    @Override
    public void onStartSettlementSelected() {
        mSettingsFragment.onStartSettlementSelected();
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
