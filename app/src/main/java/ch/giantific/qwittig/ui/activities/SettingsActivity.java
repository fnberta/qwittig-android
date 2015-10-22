package ch.giantific.qwittig.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseException;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.helpers.DeleteAccountHelper;
import ch.giantific.qwittig.helpers.LogoutHelper;
import ch.giantific.qwittig.ui.fragments.SettingsFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.AccountDeleteDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupLeaveBalanceNotZeroDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupLeaveDialogFragment;

public class SettingsActivity extends BaseActivity implements
        GroupLeaveDialogFragment.FragmentInteractionListener,
        SettingsFragment.FragmentInteractionListener,
        GroupLeaveBalanceNotZeroDialogFragment.FragmentInteractionListener,
        AccountDeleteDialogFragment.DialogInteractionListener,
        LogoutHelper.HelperInteractionListener,
        DeleteAccountHelper.HelperInteractionListener {

    private static final String SETTINGS_FRAGMENT = "settings_fragment";
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // set default Result to OK, if logOut is triggered it will be set to LOGOUT in order to
        // finish HomeActivity as well
        setResult(RESULT_OK);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment(), SETTINGS_FRAGMENT)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        findSettingsFragment();
    }

    private void findSettingsFragment() {
        mSettingsFragment = (SettingsFragment) getFragmentManager()
                .findFragmentByTag(SETTINGS_FRAGMENT);
    }

    @Override
    public void onLeaveGroupSelected() {
        mSettingsFragment.onLeaveGroupSelected();
    }

    @Override
    public void onStartSettlementSelected() {
        mSettingsFragment.onStartSettlementSelected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                mSettingsFragment.logOutUser();
                return true;
            case R.id.action_account_delete:
                mSettingsFragment.deleteAccount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLogoutSucceeded() {
        mSettingsFragment.onLogoutSucceeded();
    }

    @Override
    public void onLogoutFailed(ParseException e) {
        mSettingsFragment.onLogoutFailed(e);
    }

    @Override
    public void onDeleteAccountSelected() {
        mSettingsFragment.onDeleteAccountSelected();
    }

    @Override
    public void onDeleteUserFailed(ParseException e) {
        mSettingsFragment.onDeleteUserFailed(e);
    }
}
