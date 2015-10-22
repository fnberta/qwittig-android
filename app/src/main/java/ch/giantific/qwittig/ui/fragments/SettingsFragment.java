package ch.giantific.qwittig.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Group;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.activities.SettingsActivity;
import ch.giantific.qwittig.ui.activities.SettingsGroupNewActivity;
import ch.giantific.qwittig.ui.activities.SettingsProfileActivity;
import ch.giantific.qwittig.ui.activities.SettingsStoresActivity;
import ch.giantific.qwittig.ui.activities.SettingsUserInviteActivity;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseUtils;

public class SettingsFragment extends PreferenceFragment {

    private FragmentInteractionListener mListener;
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

        addPreferencesFromResource(R.xml.preferences);

        mCategoryMe = (PreferenceCategory) findPreference(SettingsActivity.PREF_CATEGORY_ME);
        mCategoryCurrentGroup = (PreferenceCategory)
                findPreference(SettingsActivity.PREF_CATEGORY_CURRENT_GROUP);
        mListPreferenceGroupCurrent = (ListPreference)
                findPreference(SettingsActivity.PREF_GROUP_CURRENT);
        mEditTextPreferenceGroupName = (EditTextPreference)
                findPreference(SettingsActivity.PREF_GROUP_NAME);
        mPreferenceGroupLeave = findPreference(SettingsActivity.PREF_GROUP_LEAVE);
        mPreferenceGroupLeave.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mListener.showGroupLeaveDialog();
                return true;
            }
        });
        final Preference prefProfile = findPreference(SettingsActivity.PREF_PROFILE);
        prefProfile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsProfileActivity.class);
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                startActivityForResult(intent, SettingsActivity.INTENT_REQUEST_SETTINGS_PROFILE,
                        activityOptionsCompat.toBundle());
                return true;
            }
        });
        final Preference prefStores = findPreference(SettingsActivity.PREF_STORES);
        prefStores.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsStoresActivity.class);
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                startActivity(intent, activityOptionsCompat.toBundle());
                return true;
            }
        });
        final Preference prefGroupNew = findPreference(SettingsActivity.PREF_GROUP_NEW);
        prefGroupNew.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsGroupNewActivity.class);
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                startActivityForResult(intent, SettingsActivity.INTENT_REQUEST_SETTINGS_GROUP_NEW,
                        activityOptionsCompat.toBundle());
                return true;
            }
        });
        final Preference prefGroupAddUser = findPreference(SettingsActivity.PREF_GROUP_ADD_USER);
        prefGroupAddUser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SettingsUserInviteActivity.class);
                ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                startActivity(intent, activityOptionsCompat.toBundle());
                return true;
            }
        });
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
            case SettingsActivity.INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        MessageUtils.showBasicSnackbar(rootView, getString(R.string.toast_changes_saved));
                        break;
                    case SettingsProfileActivity.RESULT_CHANGES_DISCARDED:
                        MessageUtils.showBasicSnackbar(rootView, getString(R.string.toast_changes_discarded));
                        break;
                }
                break;
            case SettingsActivity.INTENT_REQUEST_SETTINGS_GROUP_NEW:
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void showGroupLeaveDialog();
    }
}