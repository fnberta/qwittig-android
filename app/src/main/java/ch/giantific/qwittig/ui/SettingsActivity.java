package ch.giantific.qwittig.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.DeleteCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.CloudCode;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.OnlineQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.Installation;
import ch.giantific.qwittig.data.parse.models.User;
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
        CloudCode.CloudFunctionListener,
        OnlineQuery.CompensationQueryListener,
        LocalQuery.UserLocalQueryListener {

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
    private static final int UPDATE_LIST_NAME = 1;
    private static final int UPDATE_LIST_GROUP = 2;
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private ProgressDialog mProgressDialog;
    private SharedPreferences mSharedPrefs;
    private SettingsFragment mSettingsFragment;
    private User mCurrentUser;
    private Group mCurrentGroup;
    private List<ParseObject> mGroupsBalanceNotZeroOnDelete = new ArrayList<>();
    private int mSettlementsDoneCounter = 0;
    private boolean mDeleteGroup = false;

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
        String message;
        if (users.size() == 1 && users.get(0).getObjectId().equals(mCurrentUser.getObjectId())) {
            message = getString(R.string.dialog_group_leave_delete_message);
            mDeleteGroup = true;
        } else {
            message = getString(R.string.dialog_group_leave_message);
            mDeleteGroup = false;
        }

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

        final Group groupToLeaveOrDelete = mCurrentGroup;
        deleteCurrentUserFromCurrentGroup();
        if (mDeleteGroup) {
            groupToLeaveOrDelete.deleteEventually();
        }
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
        showProgressDialog(getString(R.string.progress_logout));

        resetInstallation();
        logOut();
    }

    /**
     * Unsubscribes from all notification channels and remove currentUser from installation
     * object, so that the device does not keep receiving push notifications.
     */
    private void resetInstallation() {
        ParseInstallation installation = Installation.getResetInstallation();
        installation.saveEventually();
    }

    private void logOut() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                // ignore possible exception, currentUser will always be null now
                setLoading(false);
                setResult(RESULT_LOGOUT);
                finish();
            }
        });
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
    public void startAccountDeletion() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(this)) {
            MessageUtils.showBasicSnackbar(mToolbar, getString(R.string.toast_no_connection));
            return;
        }

        showProgressDialog(getString(R.string.progress_account_delete));
        handleGroups();
    }

    private void handleGroups() {
        mGroupsBalanceNotZeroOnDelete.clear();

        List<ParseObject> userGroups = mCurrentUser.getGroups();
        if (userGroups != null && !userGroups.isEmpty()) {
            for (ParseObject group : userGroups) {
                if (!balanceIsZero(group)) {
                    mGroupsBalanceNotZeroOnDelete.add(group);
                }
            }
        }

        if (mGroupsBalanceNotZeroOnDelete.isEmpty()) {
            deleteAccount();
        } else {
            OnlineQuery.queryCompensationsForGroups(this, mGroupsBalanceNotZeroOnDelete, this);
        }
    }

    @Override
    public void onCompensationsQueryFailed(String errorMessage) {
        onParseError(errorMessage);
    }

    private void onParseError(String errorMessage) {
        setLoading(false);
        MessageUtils.showBasicSnackbar(mToolbar, errorMessage);
    }

    @Override
    public void onCompensationsQueried(List<ParseObject> compensations) {
        ParseObject.deleteAllInBackground(compensations, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(mContext, e);
                    onParseError(ParseErrorHandler.getErrorMessage(mContext, e));
                    return;
                }

                for (ParseObject group : mGroupsBalanceNotZeroOnDelete) {
                    balanceAccount(group);
                }
            }
        });
    }

    private void balanceAccount(ParseObject group) {
        CloudCode.startNewSettlement(this, group, true, this);
    }

    @Override
    public void onCloudFunctionError(ParseException e) {
        onParseError(ParseErrorHandler.getErrorMessage(this, e));
    }

    @Override
    public void onCloudFunctionReturned(String cloudFunction, Object o) {
        switch (cloudFunction) {
            case CloudCode.SETTLEMENT_NEW:
                mSettlementsDoneCounter++;

                if (mSettlementsDoneCounter == mGroupsBalanceNotZeroOnDelete.size()) {
                    deleteAccount();
                    mSettlementsDoneCounter = 0;
                }
                break;
        }
    }

    private void deleteAccount() {
        ParseInstallation installation = Installation.getResetInstallation();
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    ParseErrorHandler.handleParseError(mContext, e);
                    onParseError(ParseErrorHandler.getErrorMessage(mContext, e));
                    return;
                }

                mCurrentUser.deleteUserFields();
                mCurrentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            ParseErrorHandler.handleParseError(mContext, e);
                            onParseError(ParseErrorHandler.getErrorMessage(mContext, e));
                            return;
                        }

                        logOut();
                    }
                });
            }
        });

        // TODO: check in CloudCode if user is the only member in his groups, if yes delete groups (tricky!!)
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}
