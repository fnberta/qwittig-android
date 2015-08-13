package ch.giantific.qwittig.ui;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helper.DeleteAccountHelper;
import ch.giantific.qwittig.helper.LogoutHelper;
import ch.giantific.qwittig.ui.dialogs.AccountDeleteDialogFragment;
import ch.giantific.qwittig.ui.dialogs.GroupLeaveBalanceNotZeroDialogFragment;
import ch.giantific.qwittig.ui.dialogs.GroupLeaveDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

public class SettingsActivity extends BaseActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        GroupLeaveDialogFragment.FragmentInteractionListener,
        SettingsFragment.FragmentInteractionListener,
        GroupLeaveBalanceNotZeroDialogFragment.FragmentInteractionListener,
        AccountDeleteDialogFragment.DialogInteractionListener,
        LocalQuery.UserLocalQueryListener,
        LogoutHelper.HelperInteractionListener,
        DeleteAccountHelper.HelperInteractionListener {

    public static final String PREF_CATEGORY_ME = "pref_category_me";
    public static final String PREF_PROFILE = "pref_profile";
    public static final String PREF_STORES = "pref_stores";
    public static final String PREF_GROUP_CURRENT = "pref_group_current";
    public static final String PREF_GROUP_NEW = "pref_group_add_new";
    public static final String PREF_GROUP_NAME = "pref_group_name";
    public static final String PREF_GROUP_LEAVE = "pref_group_leave";
    public static final String PREF_GROUP_ADD_USER = "pref_group_add_user";
    public static final String PREF_CATEGORY_CURRENT_GROUP = "pref_category_current_group";
    public static final int RESULT_LOGOUT = 2;
    public static final int RESULT_GROUP_CHANGED = 3;
    private static final String SETTINGS_FRAGMENT = "settings_fragment";
    private static final String LOGOUT_HELPER = "logout_helper";
    private static final String DELETE_ACCOUNT_HELPER = "delete_account_helper";
    private static final int UPDATE_LIST_NAME = 1;
    private static final int UPDATE_LIST_GROUP = 2;
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private ProgressDialog mProgressDialog;
    private SharedPreferences mSharedPrefs;
    private SettingsFragment mSettingsFragment;
    private User mCurrentUser;
    private Group mCurrentGroup;

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

        setupPreferences();
    }

    private void setupPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateCurrentUserGroup();
        findSettingsFragment();
    }

    private void updateCurrentUserGroup() {
        mCurrentUser = (User) ParseUser.getCurrentUser();
        mCurrentGroup = mCurrentUser.getCurrentGroup();
    }

    private void findSettingsFragment() {
        mSettingsFragment = (SettingsFragment) getFragmentManager()
                .findFragmentByTag(SETTINGS_FRAGMENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PREF_GROUP_CURRENT:
                setCurrentGroupInParse(key);
                break;
            case PREF_GROUP_NAME:
                changeCurrentGroupName(key);
                break;
        }
    }

    /**
     * updates the current group in parse database
     *
     * @param key
     */
    private void setCurrentGroupInParse(String key) {
        String newCurrentGroupId = mSharedPrefs.getString(key, "");
        if (TextUtils.isEmpty(newCurrentGroupId)) {
            return;
        }

        Group newCurrentGroup = (Group) ParseObject.createWithoutData(Group.CLASS, newCurrentGroupId);
        mCurrentUser.setCurrentGroup(newCurrentGroup);
        mCurrentUser.saveEventually();

        updateCurrentUserGroup();
        updateSettingsFragment(UPDATE_LIST_GROUP);

        // NavDrawer group setting needs to be updated
        setResult(RESULT_GROUP_CHANGED);
    }

    private void changeCurrentGroupName(String key) {
        String oldName = mCurrentGroup.getName();
        String newName = mSharedPrefs.getString(key, oldName);

        if (!oldName.equals(newName)) {
            mCurrentGroup.setName(newName);
            mCurrentGroup.saveEventually();

            // update group list in SettingsFragment
            updateSettingsFragment(UPDATE_LIST_NAME);
        }
    }

    private void updateSettingsFragment(int preference) {
        switch (preference) {
            case UPDATE_LIST_NAME:
                mSettingsFragment.setupCurrentGroup();
                mSettingsFragment.setupCurrentGroupCategory();
                break;
            case UPDATE_LIST_GROUP:
                mSettingsFragment.updateCurrentUserAndGroup();
                mSettingsFragment.setupCurrentGroup();
                mSettingsFragment.setupCurrentGroupCategory();
                break;
        }
    }

    @Override
    public void showGroupLeaveDialog() {
        // check if user is the only one in the group, if yes delete it
        LocalQuery.queryUsers(this);
    }

    @Override
    public void onUsersLocalQueried(List<ParseUser> users) {
        String message = users.size() == 1 &&
                users.get(0).getObjectId().equals(mCurrentUser.getObjectId()) ?
                getString(R.string.dialog_group_leave_delete_message) :
                getString(R.string.dialog_group_leave_message);

        GroupLeaveDialogFragment groupLeaveDialogFragment =
                GroupLeaveDialogFragment.newInstance(message);
        groupLeaveDialogFragment.show(getFragmentManager(), "group_leave");
    }

    /**
     * Called from GroupLeaveDialog. Deletes the currentUser from the currentGroup.
     */
    @Override
    public void leaveCurrentGroup() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            showAccountCreateDialog();
            return;
        }

        if (!balanceIsZero(mCurrentGroup)) {
            showBalanceNotZeroDialog();
            return;
        }

        deleteCurrentUserFromCurrentGroup();
    }

    private void deleteCurrentUserFromCurrentGroup() {
        mCurrentUser.removeGroup(mCurrentGroup);
        ParsePush.unsubscribeInBackground(mCurrentGroup.getObjectId());

        // fall back to first group in the list
        List<ParseObject> groups = mCurrentUser.getGroups();
        if (groups.size() > 0) {
            mSettingsFragment.setGroupCurrentValue(groups.get(0).getObjectId());
        } else {
            mCurrentUser.removeCurrentGroup();
            updateCurrentUserGroup();

            // update group list in SettingsFragment
            updateSettingsFragment(UPDATE_LIST_GROUP);
        }
        mCurrentUser.saveEventually();

        // NavDrawer group setting needs to be updated
        setResult(RESULT_GROUP_CHANGED);
    }

    private boolean balanceIsZero(ParseObject group) {
        BigFraction balance = mCurrentUser.getBalance(group);

        return balance.equals(BigFraction.ZERO);
    }

    private void showBalanceNotZeroDialog() {
        GroupLeaveBalanceNotZeroDialogFragment groupLeaveBalanceNotZeroDialogFragment =
                new GroupLeaveBalanceNotZeroDialogFragment();
        groupLeaveBalanceNotZeroDialogFragment.show(getFragmentManager(),
                "group_leave_balance_not_zero");
    }

    /**
     * Callback from dialog when user decides to create a new settlement
     */
    @Override
    public void startNewSettlement() {
        Intent intent = new Intent(this, CompensationsActivity.class);
        intent.putExtra(CompensationsActivity.INTENT_AUTO_START_NEW, true);
        startActivity(intent);
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
                logOutUser();
                return true;
            case R.id.action_account_delete:
                if (ParseUtils.isTestUser(mCurrentUser)) {
                    showAccountCreateDialog();
                } else {
                    showAccountDeleteDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showProgressDialog(String message) {
        mProgressDialog = MessageUtils.getProgressDialog(this, message);
        mProgressDialog.show();
    }

    private void logOutUser() {
        if (!Utils.isConnected(this)) {
            MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_no_connection));
            return;
        }

        showProgressDialog(getString(R.string.progress_logout));
        logOutWithHelper();
    }

    private void logOutWithHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        LogoutHelper logoutHelper = findLogoutHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (logoutHelper == null) {
            logoutHelper = new LogoutHelper();

            fragmentManager.beginTransaction()
                    .add(logoutHelper, LOGOUT_HELPER)
                    .commit();
        }
    }

    private LogoutHelper findLogoutHelper(FragmentManager fragmentManager) {
        return (LogoutHelper) fragmentManager.findFragmentByTag(LOGOUT_HELPER);
    }

    @Override
    public void onLogoutSucceeded() {
        setLoading(false);
        setResult(RESULT_LOGOUT);
        finish();
    }

    @Override
    public void onLogoutFailed(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        onParseError(ParseErrorHandler.getErrorMessage(this, e));
        removeLoginHelper();
    }

    private void onParseError(String errorMessage) {
        setLoading(false);
        MessageUtils.showBasicSnackbar(mToolbar, errorMessage);
    }

    private void removeLoginHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        LogoutHelper loginHelper = findLogoutHelper(fragmentManager);

        if (loginHelper != null) {
            fragmentManager.beginTransaction().remove(loginHelper).commitAllowingStateLoss();
        }
    }

    private void setLoading(boolean isLoading) {
        if (!isLoading && mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showAccountDeleteDialog() {
        AccountDeleteDialogFragment accountDeleteDialogFragment =
                new AccountDeleteDialogFragment();
        accountDeleteDialogFragment.show(getFragmentManager(), "account_delete");
    }

    /**
     * Called from account delete confirmation dialog to start AccountDeletion process.
     */
    @Override
    public void deleteAccount() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(this)) {
            MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_no_connection));
            return;
        }

        showProgressDialog(getString(R.string.progress_account_delete));
        deleteAccountWithHelper();
    }

    private void deleteAccountWithHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        DeleteAccountHelper deleteAccountHelper = findDeleteAccountHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (deleteAccountHelper == null) {
            deleteAccountHelper = new DeleteAccountHelper();

            fragmentManager.beginTransaction()
                    .add(deleteAccountHelper, DELETE_ACCOUNT_HELPER)
                    .commit();
        }
    }

    private DeleteAccountHelper findDeleteAccountHelper(FragmentManager fragmentManager) {
        return (DeleteAccountHelper) fragmentManager.findFragmentByTag(DELETE_ACCOUNT_HELPER);
    }

    private void removeDeleteAccountHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        DeleteAccountHelper deleteAccountHelper = findDeleteAccountHelper(fragmentManager);

        if (deleteAccountHelper != null) {
            fragmentManager.beginTransaction().remove(deleteAccountHelper).commitAllowingStateLoss();
        }
    }

    @Override
    public void onDeleteUserFailed(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        onParseError(ParseErrorHandler.getErrorMessage(this, e));
        removeDeleteAccountHelper();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}
