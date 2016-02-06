/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;

import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.repositories.ParseApiRepository;
import ch.giantific.qwittig.data.repositories.ParseUserRepository;
import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.fragments.ConfirmationDialogFragment;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;
import rx.SingleSubscriber;

/**
 * Displays the main settings of the app and listens for changes.
 * <p/>
 * Subclass of {@link PreferenceFragmentCompat}.
 * <p/>
 * Implements {@link SharedPreferences.OnSharedPreferenceChangeListener}.
 */
@SuppressWarnings("unchecked")
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int RESULT_LOGOUT = 2;
    public static final int RESULT_GROUP_CHANGED = 3;
    private static final String PREF_CATEGORY_ME = "PREF_CATEGORY_ME";
    private static final String PREF_PROFILE = "PREF_PROFILE";
    private static final String PREF_STORES = "PREF_STORES";
    private static final String PREF_GROUP_CURRENT = "PREF_GROUP_CURRENT";
    private static final String PREF_GROUP_ADD_NEW = "PREF_GROUP_ADD_NEW";
    private static final String PREF_CATEGORY_CURRENT_GROUP = "PREF_CATEGORY_CURRENT_GROUP";
    private static final String PREF_GROUP_NAME = "PREF_GROUP_NAME";
    private static final String PREF_GROUP_LEAVE = "PREF_GROUP_LEAVE";
    private static final String PREF_GROUP_ADD_USER = "PREF_GROUP_ADD_USER";
    private static final String LOGOUT_WORKER = "LOGOUT_WORKER";
    private static final int UPDATE_LIST_NAME = 1;
    private static final int UPDATE_LIST_GROUP = 2;
    private UserRepository mUserRepo;
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
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mUserRepo = new ParseUserRepository(new ParseApiRepository(new Gson()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        final FragmentActivity activity = getActivity();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false);
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
                final Intent intent = new Intent(activity, SettingsProfileActivity.class);
                final ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                activity.startActivityForResult(intent, SettingsActivity.INTENT_REQUEST_SETTINGS_PROFILE,
                        activityOptionsCompat.toBundle());
                return true;
            }
        });
        final Preference prefGroupNew = findPreference(PREF_GROUP_ADD_NEW);
        prefGroupNew.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Intent intent = new Intent(activity, SettingsGroupNewActivity.class);
                final ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                activity.startActivityForResult(intent, SettingsActivity.INTENT_REQUEST_SETTINGS_GROUP_NEW,
                        activityOptionsCompat.toBundle());
                return true;
            }
        });
        final Preference prefGroupAddUser = findPreference(PREF_GROUP_ADD_USER);
        prefGroupAddUser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(activity, SettingsUserInviteActivity.class);
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                activity.startActivity(intent, activityOptionsCompat.toBundle());
                return true;
            }
        });
    }

    /**
     * Checks if the user is the last one in the group, if yes, tells him/her that the group will
     * be deleted.
     */
    private void showGroupLeaveDialog() {
//        mUserRepo.getIdentitiesLocalAsync(mCurrentGroup)
//                .toList()
//                .toSingle()
//                .subscribe(new SingleSubscriber<List<User>>() {
//                    @Override
//                    public void onSuccess(List<User> users) {
//                        String message = users.size() == 1 &&
//                                users.get(0).getObjectId().equals(mCurrentUser.getObjectId()) ?
//                                getString(R.string.dialog_group_leave_delete_message) :
//                                getString(R.string.dialog_group_leave_message);
//
//                        ConfirmationDialogFragment.display(getFragmentManager(), message,
//                                R.string.dialog_positive_leave);
//                    }
//
//                    @Override
//                    public void onError(Throwable error) {
//                        // TODO: handle error
//                    }
//                });
    }

    /**
     * Removes the current user from the current group if he/she is not a test user and his/her
     * balance is zero.
     */
    public void onLeaveGroupSelected() {
        if (!balanceIsZero(mCurrentGroup)) {
            Snackbar.make(getView(), R.string.toast_balance_not_zero, Snackbar.LENGTH_LONG).show();
            return;
        }

        deleteCurrentUserFromCurrentGroup();
    }

    private boolean balanceIsZero(ParseObject group) {
        final BigFraction balance = mCurrentUser.getCurrentIdentity().getBalance();
        return balance.equals(BigFraction.ZERO);
    }

    private void deleteCurrentUserFromCurrentGroup() {
//        mCurrentUser.removeGroup(mCurrentGroup);
//        ParsePush.unsubscribeInBackground(mCurrentGroup.getObjectId());
//
//        // fall back to first group in the list
//        List<ParseObject> groups = mCurrentUser.getGroups();
//        if (groups.size() > 0) {
//            setGroupCurrentValue(groups.get(0).getObjectId());
//        } else {
//            mCurrentUser.removeCurrentGroup();
//            updateCurrentUserAndGroup();
//
//            // update group list
//            updateSettings(UPDATE_LIST_GROUP);
//        }
//        mCurrentUser.saveEventually();
//
//        // NavDrawer group setting needs to be updated
//        getActivity().setResult(RESULT_GROUP_CHANGED);
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

    @Override
    public void onStart() {
        super.onStart();

        updateCurrentUserAndGroup();

        // setup preferences
        setupCurrentGroup();
        setupCurrentGroupCategory();
    }

    private void updateCurrentUserAndGroup() {
        mCurrentUser = (User) ParseUser.getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentGroup = mCurrentUser.getCurrentIdentity().getGroup();
        }
    }

    /**
     * Sets up the current group list preference. Sets the entries and values and calls generic
     * method that handles correct display of default value and summary.
     */
    private void setupCurrentGroup() {
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
//        List<ParseObject> groups = mCurrentUser.getGroups();
//
//        if (!groups.isEmpty()) {
//            int groupsSize = groups.size();
//            final List<String> groupsEntries = new ArrayList<>(groupsSize);
//            final List<String> groupsValues = new ArrayList<>(groupsSize);
//
//            for (ParseObject parseObject : groups) {
//                Group group = (Group) parseObject;
//                groupsEntries.add(group.getName());
//                groupsValues.add(group.getObjectId());
//            }
//
//            mCurrentUserGroupsEntries = groupsEntries.toArray(new CharSequence[groupsSize]);
//            mCurrentUserGroupsValues = groupsValues.toArray(new CharSequence[groupsSize]);
//        } else {
//            mCurrentUserGroupsEntries = new CharSequence[0];
//            mCurrentUserGroupsValues = new CharSequence[0];
//        }
    }

    private void setupListPreference(@NonNull final ListPreference pref,
                                     @Nullable String selectedValue) {
        // Set selected value from parse.com database.
        if (selectedValue != null) {
            pref.setValue(selectedValue);
        }

        // Set summary to the current Entry
        pref.setSummary(pref.getEntry());

        // Update the summary with currently selected value
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, @NonNull Object o) {
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

    private void setupCurrentGroupCategory() {
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
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (showPreferences) {
            preferenceScreen.addPreference(mCategoryCurrentGroup);
            mCategoryMe.addPreference(mListPreferenceGroupCurrent);
        } else {
            preferenceScreen.removePreference(mCategoryCurrentGroup);
            mCategoryMe.removePreference(mListPreferenceGroupCurrent);
        }
    }

    private void setupGroupChangeName() {
        mEditTextPreferenceGroupName.setText(mCurrentGroup.getName());
    }

    private void setupGroupLeave() {
        mPreferenceGroupLeave.setTitle(getString(R.string.pref_group_leave_group, mCurrentGroup.getName()));
    }

    private void setGroupCurrentValue(String value) {
        mListPreferenceGroupCurrent.setValue(value);
    }

    @Override
    public void onResume() {
        super.onResume();

        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @NonNull String key) {
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
//        String newCurrentGroupId = mSharedPrefs.getString(key, "");
//        if (TextUtils.isEmpty(newCurrentGroupId)) {
//            return;
//        }
//
//        Group newCurrentGroup = (Group) ParseObject.createWithoutData(Group.CLASS, newCurrentGroupId);
//        mCurrentUser.setCurrentGroup(newCurrentGroup);
//        mCurrentUser.saveEventually();
//
//        updateCurrentUserAndGroup();
//        updateSettings(UPDATE_LIST_GROUP);
//
//        // NavDrawer group setting needs to be updated
//        getActivity().setResult(RESULT_GROUP_CHANGED);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOutUser();
                return true;
            case R.id.action_account_delete:
                deleteAccount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Logs the current user out with the help of a retained worker fragment.
     */
    public void logOutUser() {
        if (!Utils.isNetworkAvailable(getActivity())) {
            Snackbar.make(getView(), R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
            return;
        }

        showProgressDialog(getString(R.string.progress_logout));
        logOutWithWorker(false);
    }

    private void showProgressDialog(@NonNull String message) {
        mProgressDialog = ProgressDialog.show(getActivity(), null, message);
    }

    private void logOutWithWorker(boolean deleteUser) {
        final FragmentManager fragmentManager = getFragmentManager();
        Fragment logoutWorker = fragmentManager.findFragmentByTag(LOGOUT_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (logoutWorker == null) {
            logoutWorker = LogoutWorker.newInstance(deleteUser);

            fragmentManager.beginTransaction()
                    .add(logoutWorker, LOGOUT_WORKER)
                    .commit();
        }
    }

    /**
     * Hides the {@link ProgressDialog}, sets the activity result to logout and finishes.
     */
    public void onLoggedOut() {
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
     * Handles a failed logout attempt by showing the user the error, removing the worker
     * fragment and removing any progress indicators.
     *
     * @param errorMessage the error message from the exception thrown during the logout attempt
     */
    public void onLogoutFailed(@StringRes int errorMessage) {
        dismissProgressDialog();
        WorkerUtils.removeWorker(getFragmentManager(), LOGOUT_WORKER);

        Snackbar.make(getView(), errorMessage, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Shows a dialog that asks the user if he/she really wants to delete the account.
     */
    public void deleteAccount() {
        AccountDeleteDialogFragment.display(getFragmentManager());
    }

    /**
     * Deletes the account of a user by using a retained worker fragment for the task.
     */
    public void onDeleteAccountSelected() {
        if (!Utils.isNetworkAvailable(getActivity())) {
            Snackbar.make(getView(), R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
            return;
        }

        showProgressDialog(getString(R.string.progress_account_delete));
        logOutWithWorker(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}