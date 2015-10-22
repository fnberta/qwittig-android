package ch.giantific.qwittig.ui.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.view.View;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.LocalQuery;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.helpers.DeleteAccountHelper;
import ch.giantific.qwittig.helpers.LogoutHelper;
import ch.giantific.qwittig.ui.activities.BaseActivity;
import ch.giantific.qwittig.ui.activities.FinanceActivity;
import ch.giantific.qwittig.ui.activities.SettingsActivity;
import ch.giantific.qwittig.ui.activities.SettingsGroupNewActivity;
import ch.giantific.qwittig.ui.activities.SettingsProfileActivity;
import ch.giantific.qwittig.ui.activities.SettingsStoresActivity;
import ch.giantific.qwittig.ui.activities.SettingsUserInviteActivity;
import ch.giantific.qwittig.ui.fragments.dialogs.AccountDeleteDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupLeaveBalanceNotZeroDialogFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.GroupLeaveDialogFragment;
import ch.giantific.qwittig.utils.HelperUtils;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_STORES = "pref_stores";
    public static final String PREF_GROUP_CURRENT = "pref_group_current";
    public static final String PREF_GROUP_NEW = "pref_group_add_new";
    public static final String PREF_GROUP_NAME = "pref_group_name";
    public static final String PREF_GROUP_LEAVE = "pref_group_leave";
    public static final String PREF_GROUP_ADD_USER = "pref_group_add_user";
    public static final String PREF_CATEGORY_CURRENT_GROUP = "pref_category_current_group";
    public static final String GROUP_LEAVE_DIALOG = "group_leave";
    public static final String GROUP_LEAVE_BALANCE_NOT_ZERO_DIALOG = "group_leave_balance_not_zero";
    public static final int RESULT_LOGOUT = 2;
    public static final int RESULT_GROUP_CHANGED = 3;
    public static final String ACCOUNT_DELETE_DIALOG = "account_delete";
    private static final String PREF_PROFILE = "pref_profile";
    private static final String PREF_CATEGORY_ME = "pref_category_me";
    private static final String LOGOUT_HELPER = "logout_helper";
    private static final String DELETE_ACCOUNT_HELPER = "delete_account_helper";
    private static final int UPDATE_LIST_NAME = 1;
    private static final int UPDATE_LIST_GROUP = 2;
    private FragmentInteractionListener mListener;
    private ProgressDialog mProgressDialog;
    private SharedPreferences mSharedPrefs;
    private PreferenceCategory mCategoryMe;
    private PreferenceCategory mCategoryCurrentGroup;
    private ListPreference mListPreferenceGroupCurrent;
    private EditTextPreference mEditTextPreferenceGroupName;
    private Preference mPreferenceGroupLeave;
    private CharSequence[] mCurrentUserGroupsEntries;
    private CharSequence[] mCurrentUserGroupsValues;
    private User mCurrentUser;
    private Group mCurrentGroup;

    public SettingsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getActivity();
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        addPreferencesFromResource(R.xml.preferences);

        mCategoryMe = (PreferenceCategory) findPreference(PREF_CATEGORY_ME);
        mCategoryCurrentGroup = (PreferenceCategory)
                findPreference(PREF_CATEGORY_CURRENT_GROUP);
        mListPreferenceGroupCurrent = (ListPreference)
                findPreference(PREF_GROUP_CURRENT);
        mEditTextPreferenceGroupName = (EditTextPreference)
                findPreference(PREF_GROUP_NAME);
        mPreferenceGroupLeave = findPreference(PREF_GROUP_LEAVE);
        mPreferenceGroupLeave.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showGroupLeaveDialog();
                return true;
            }
        });
        final Preference prefProfile = findPreference(PREF_PROFILE);
        prefProfile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(context, SettingsProfileActivity.class);
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                startActivityForResult(intent, SettingsActivity.INTENT_REQUEST_SETTINGS_PROFILE,
                        activityOptionsCompat.toBundle());
                return true;
            }
        });
        final Preference prefStores = findPreference(PREF_STORES);
        prefStores.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(context, SettingsStoresActivity.class);
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                startActivity(intent, activityOptionsCompat.toBundle());
                return true;
            }
        });
        final Preference prefGroupNew = findPreference(PREF_GROUP_NEW);
        prefGroupNew.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(context, SettingsGroupNewActivity.class);
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                startActivityForResult(intent, SettingsActivity.INTENT_REQUEST_SETTINGS_GROUP_NEW,
                        activityOptionsCompat.toBundle());
                return true;
            }
        });
        final Preference prefGroupAddUser = findPreference(PREF_GROUP_ADD_USER);
        prefGroupAddUser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(context, SettingsUserInviteActivity.class);
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                startActivity(intent, activityOptionsCompat.toBundle());
                return true;
            }
        });
    }

    private void showGroupLeaveDialog() {
        // check if user is the only one in the group, if yes delete the group
        LocalQuery.queryUsers(new LocalQuery.UserLocalQueryListener() {
            @Override
            public void onUsersLocalQueried(List<ParseUser> users) {
                String message = users.size() == 1 &&
                        users.get(0).getObjectId().equals(mCurrentUser.getObjectId()) ?
                        getString(R.string.dialog_group_leave_delete_message) :
                        getString(R.string.dialog_group_leave_message);

                GroupLeaveDialogFragment groupLeaveDialogFragment =
                        GroupLeaveDialogFragment.newInstance(message);
                groupLeaveDialogFragment.show(getFragmentManager(), GROUP_LEAVE_DIALOG);
            }
        });
    }

    /**
     * Deletes the currentUser from the currentGroup.
     */
    public void onLeaveGroupSelected() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (!balanceIsZero(mCurrentGroup)) {
            showBalanceNotZeroDialog();
            return;
        }

        deleteCurrentUserFromCurrentGroup();
    }

    private boolean balanceIsZero(ParseObject group) {
        BigFraction balance = mCurrentUser.getBalance(group);

        return balance.equals(BigFraction.ZERO);
    }


    private void showBalanceNotZeroDialog() {
        GroupLeaveBalanceNotZeroDialogFragment groupLeaveBalanceNotZeroDialogFragment =
                new GroupLeaveBalanceNotZeroDialogFragment();
        groupLeaveBalanceNotZeroDialogFragment.show(getFragmentManager(),
                GROUP_LEAVE_BALANCE_NOT_ZERO_DIALOG);
    }

    private void deleteCurrentUserFromCurrentGroup() {
        mCurrentUser.removeGroup(mCurrentGroup);
        ParsePush.unsubscribeInBackground(mCurrentGroup.getObjectId());

        // fall back to first group in the list
        List<ParseObject> groups = mCurrentUser.getGroups();
        if (groups.size() > 0) {
            setGroupCurrentValue(groups.get(0).getObjectId());
        } else {
            mCurrentUser.removeCurrentGroup();
            updateCurrentUserAndGroup();

            // update group list
            updateSettings(UPDATE_LIST_GROUP);
        }
        mCurrentUser.saveEventually();

        // NavDrawer group setting needs to be updated
        getActivity().setResult(RESULT_GROUP_CHANGED);
    }

    private void updateSettings(int preference) {
        switch (preference) {
            case UPDATE_LIST_GROUP:
                updateCurrentUserAndGroup();
                // fall through
            case UPDATE_LIST_NAME:
                setupCurrentGroup();
                setupCurrentGroupCategory();
                break;
        }
    }

    /**
     * Starts a new settlement in reaction to the user wanting to leave the group with a non-zero
     * balance.
     */
    public void onStartSettlementSelected() {
        Intent intent = new Intent(getActivity(), FinanceActivity.class);
        intent.putExtra(FinanceActivity.INTENT_AUTO_START_NEW, true);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        updateCurrentUserAndGroup();

        // setup preferences
        setupCurrentGroup();
        setupCurrentGroupCategory();
    }

    public void updateCurrentUserAndGroup() {
        mCurrentUser = (User) ParseUser.getCurrentUser();
        mCurrentGroup = mCurrentUser.getCurrentGroup();
    }

    /**
     * Sets up the current group list preference. Sets the entries and values and calls generic
     * method that handles correct display of default value and summary.
     */
    public void setupCurrentGroup() {
        setCurrentUserGroupsList(); // set new values

        // Define selected value from parse.com database
        String selectedValue = null;
        if (mCurrentGroup != null) {
            selectedValue = mCurrentGroup.getObjectId();
        }

        // Set Entries and Values
        mListPreferenceGroupCurrent.setEntries(mCurrentUserGroupsEntries);
        mListPreferenceGroupCurrent.setEntryValues(mCurrentUserGroupsValues);

        // Setup default value and summary.
        setupListPreference(mListPreferenceGroupCurrent, selectedValue);
    }

    /**
     * Gets the groups of the currentUser and puts them in arrays to be used in the settings.
     */
    private void setCurrentUserGroupsList() {
        List<ParseObject> groups = mCurrentUser.getGroups();

        if (!groups.isEmpty()) {
            int groupsSize = groups.size();
            final List<String> groupsEntries = new ArrayList<>(groupsSize);
            final List<String> groupsValues = new ArrayList<>(groupsSize);

            for (ParseObject parseObject : groups) {
                Group group = (Group) parseObject;
                groupsEntries.add(group.getName());
                groupsValues.add(group.getObjectId());
            }

            mCurrentUserGroupsEntries = groupsEntries.toArray(new CharSequence[groupsSize]);
            mCurrentUserGroupsValues = groupsValues.toArray(new CharSequence[groupsSize]);
        } else {
            mCurrentUserGroupsEntries = new CharSequence[0];
            mCurrentUserGroupsValues = new CharSequence[0];
        }
    }

    private void setupListPreference(final ListPreference pref, String selectedValue) {
        // Set selected value from parse.com database.
        if (selectedValue != null) {
            pref.setValue(selectedValue);
        }

        // Set summary to the current Entry
        pref.setSummary(pref.getEntry());

        // Update the summary with currently selected value
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                int index = pref.findIndexOfValue(o.toString());

                if (index >= 0) {
                    preference.setSummary(pref.getEntries()[index]);
                } else {
                    preference.setSummary(null);
                }
                return true;
            }
        });
    }

    public void setupCurrentGroupCategory() {
        if (mCurrentGroup != null) {
            mCategoryCurrentGroup.setTitle(mCurrentGroup.getName());
            setCurrentGroupPreferencesVisibility(true);

            setupGroupChangeName();
            setupGroupLeave();
        } else {
            setCurrentGroupPreferencesVisibility(false);
        }
    }

    private void setCurrentGroupPreferencesVisibility(boolean showPreferences) {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (showPreferences) {
            preferenceScreen.addPreference(mCategoryCurrentGroup);
            mCategoryMe.addPreference(mListPreferenceGroupCurrent);
        } else {
            preferenceScreen.removePreference(mCategoryCurrentGroup);
            mCategoryMe.removePreference(mListPreferenceGroupCurrent);
        }
    }

    private void setupGroupChangeName() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mEditTextPreferenceGroupName.setEnabled(false);
        }
        mEditTextPreferenceGroupName.setText(mCurrentGroup.getName());
    }

    private void setupGroupLeave() {
        mPreferenceGroupLeave.setTitle(getString(R.string.pref_group_leave_group, mCurrentGroup.getName()));
    }

    public void setGroupCurrentValue(String value) {
        mListPreferenceGroupCurrent.setValue(value);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final View rootView = getView();

        switch (requestCode) {
            case BaseActivity.INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        MessageUtils.showBasicSnackbar(rootView, getString(R.string.toast_changes_saved));
                        break;
                    case SettingsProfileActivity.RESULT_CHANGES_DISCARDED:
                        MessageUtils.showBasicSnackbar(rootView, getString(R.string.toast_changes_discarded));
                        break;
                }
                break;
            case BaseActivity.INTENT_REQUEST_SETTINGS_GROUP_NEW:
                if (resultCode == Activity.RESULT_OK) {
                    String newGroupName = data.getStringExtra(
                            SettingsGroupNewActivity.RESULT_DATA_GROUP);
                    MessageUtils.showBasicSnackbar(rootView, getString(R.string.toast_group_added,
                            newGroupName));
                }
                break;
        }
    }

    @Override
    public void onResume() {
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

    private void setCurrentGroupInParse(String key) {
        String newCurrentGroupId = mSharedPrefs.getString(key, "");
        if (TextUtils.isEmpty(newCurrentGroupId)) {
            return;
        }

        Group newCurrentGroup = (Group) ParseObject.createWithoutData(Group.CLASS, newCurrentGroupId);
        mCurrentUser.setCurrentGroup(newCurrentGroup);
        mCurrentUser.saveEventually();

        updateCurrentUserAndGroup();
        updateSettings(UPDATE_LIST_GROUP);

        // NavDrawer group setting needs to be updated
        getActivity().setResult(RESULT_GROUP_CHANGED);
    }

    private void changeCurrentGroupName(String key) {
        String oldName = mCurrentGroup.getName();
        String newName = mSharedPrefs.getString(key, oldName);

        if (!oldName.equals(newName)) {
            mCurrentGroup.setName(newName);
            mCurrentGroup.saveEventually();

            // update group list in SettingsFragment
            updateSettings(UPDATE_LIST_NAME);
        }
    }

    /**
     * Starts new helper fragment to log out the user.
     */
    public void logOutUser() {
        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(getView(), getString(R.string.toast_no_connection));
            return;
        }

        showProgressDialog(getString(R.string.progress_logout));
        logOutWithHelper();
    }

    private void showProgressDialog(String message) {
        mProgressDialog = MessageUtils.getProgressDialog(getActivity(), message);
        mProgressDialog.show();
    }

    private void logOutWithHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment logoutHelper = HelperUtils.findHelper(fragmentManager, LOGOUT_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (logoutHelper == null) {
            logoutHelper = new LogoutHelper();

            fragmentManager.beginTransaction()
                    .add(logoutHelper, LOGOUT_HELPER)
                    .commit();
        }
    }

    /**
     * Handles a successful logout attempt. Hides the progress bar and finishes.
     */
    public void onLogoutSucceeded() {
        dismissProgressDialog();
        getActivity().setResult(RESULT_LOGOUT);
        getActivity().finish();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Handles a failed logout attempt. Passes the exception to the generic Parse error handler
     * and removes the helper fragment.
     *
     * @param e the ParseException thrown during the logout attempt
     */
    public void onLogoutFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        onParseError(ParseErrorHandler.getErrorMessage(getActivity(), e));
        HelperUtils.removeHelper(getFragmentManager(), LOGOUT_HELPER);
    }

    private void onParseError(String errorMessage) {
        dismissProgressDialog();
        MessageUtils.showBasicSnackbar(getView(), errorMessage);
    }

    public void deleteAccount() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
        } else {
            showAccountDeleteDialog();
        }
    }

    private void showAccountDeleteDialog() {
        AccountDeleteDialogFragment accountDeleteDialogFragment =
                new AccountDeleteDialogFragment();
        accountDeleteDialogFragment.show(getFragmentManager(), ACCOUNT_DELETE_DIALOG);
    }

    /**
     * Deletes the account of a user by using a helper fragment for the task.
     */
    public void onDeleteAccountSelected() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        if (!Utils.isConnected(getActivity())) {
            MessageUtils.showBasicSnackbar(getView(), getString(R.string.toast_no_connection));
            return;
        }

        showProgressDialog(getString(R.string.progress_account_delete));
        deleteAccountWithHelper();
    }

    private void deleteAccountWithHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment deleteAccountHelper = HelperUtils.findHelper(fragmentManager, DELETE_ACCOUNT_HELPER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (deleteAccountHelper == null) {
            deleteAccountHelper = new DeleteAccountHelper();

            fragmentManager.beginTransaction()
                    .add(deleteAccountHelper, DELETE_ACCOUNT_HELPER)
                    .commit();
        }
    }

    /**
     * Handles the failed attempt to delete a user. Passes the error to the generic Parse error
     * handler, hides the progress bar, removes the helper fragment and shows the user the error.
     *
     * @param e the ParseException thrown during the delete attempt
     */
    public void onDeleteUserFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        onParseError(ParseErrorHandler.getErrorMessage(getActivity(), e));
        HelperUtils.removeHelper(getFragmentManager(), DELETE_ACCOUNT_HELPER);
    }

    @Override
    public void onPause() {
        super.onPause();

        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener extends BaseFragment.BaseFragmentInteractionListener {
    }
}