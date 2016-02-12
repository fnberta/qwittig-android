/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;


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
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.di.components.DaggerSettingsComponent;
import ch.giantific.qwittig.di.modules.SettingsViewModelModule;
import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.presentation.common.fragments.ConfirmationDialogFragment;
import ch.giantific.qwittig.presentation.settings.addgroup.SettingsAddGroupActivity;
import ch.giantific.qwittig.presentation.settings.addusers.SettingsAddUsersActivity;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileActivity;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;

/**
 * Displays the main settings of the app and listens for changes.
 * <p/>
 * Subclass of {@link PreferenceFragmentCompat}.
 * <p/>
 * Implements {@link SharedPreferences.OnSharedPreferenceChangeListener}.
 */
@SuppressWarnings("unchecked")
public class SettingsFragment extends PreferenceFragmentCompat
        implements SettingsViewModel.ViewListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PREF_PROFILE = "PREF_PROFILE";
    private static final String PREF_GROUP_CURRENT = "PREF_GROUP_CURRENT";
    private static final String PREF_GROUP_ADD_NEW = "PREF_GROUP_ADD_NEW";
    private static final String PREF_CATEGORY_CURRENT_GROUP = "PREF_CATEGORY_CURRENT_GROUP";
    private static final String PREF_GROUP_NAME = "PREF_GROUP_NAME";
    private static final String PREF_GROUP_LEAVE = "PREF_GROUP_LEAVE";
    private static final String PREF_GROUP_ADD_USER = "PREF_GROUP_ADD_USER";
    @Inject
    SettingsViewModel mViewModel;
    @Inject
    SharedPreferences mSharedPrefs;
    private ActivityListener mActivity;
    private ProgressDialog mProgressDialog;
    private PreferenceCategory mCategoryCurrentGroup;
    private ListPreference mListPreferenceGroupCurrent;
    private EditTextPreference mEditTextPreferenceGroupName;
    private Preference mPreferenceGroupLeave;

    public SettingsFragment() {
        // required empty constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mActivity = (ActivityListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ActivityListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        DaggerSettingsComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .settingsViewModelModule(new SettingsViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);

        mViewModel.onPreferencesLoaded();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        final FragmentActivity activity = getActivity();
        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);

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
                mViewModel.onLeaveGroupClick();
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
                final Intent intent = new Intent(activity, SettingsAddGroupActivity.class);
                final ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                activity.startActivityForResult(intent, SettingsActivity.INTENT_REQUEST_SETTINGS_ADD_GROUP,
                        activityOptionsCompat.toBundle());
                return true;
            }
        });
        final Preference prefGroupAddUser = findPreference(PREF_GROUP_ADD_USER);
        prefGroupAddUser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Intent intent = new Intent(activity, SettingsAddUsersActivity.class);
                final ActivityOptionsCompat activityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                activity.startActivity(intent, activityOptionsCompat.toBundle());
                return true;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mViewModel.saveState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity.setSettingsViewModel(mViewModel);
    }

    @Override
    public void onStart() {
        super.onStart();

        mViewModel.onStart();
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
                final String groupId = mSharedPrefs.getString(key, "");
                mViewModel.onGroupSelected(groupId);
                break;
            case PREF_GROUP_NAME:
                final String name = mSharedPrefs.getString(key, "");
                mViewModel.onGroupNameChanged(name);
                break;
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
                mViewModel.onLogoutMenuClick();
                return true;
            case R.id.action_account_delete:
                mViewModel.onDeleteAccountMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mViewModel.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    public void setupGroupSelection(@NonNull CharSequence[] entries, @NonNull CharSequence[] values,
                                    @NonNull String selectedValue) {
        mListPreferenceGroupCurrent.setEntries(entries);
        mListPreferenceGroupCurrent.setEntryValues(values);
        mListPreferenceGroupCurrent.setValue(selectedValue);
        setupListPreference(mListPreferenceGroupCurrent, selectedValue);
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

    @Override
    public void setCurrentGroupTitle(@NonNull String title) {
        mCategoryCurrentGroup.setTitle(title);
    }

    @Override
    public void setChangeGroupNameText(@NonNull String text) {
        mEditTextPreferenceGroupName.setText(text);
    }

    @Override
    public void setLeaveGroupTitle(@StringRes int message, @NonNull String groupName) {
        mPreferenceGroupLeave.setTitle(getString(message, groupName));
    }

    @Override
    public void loadLogoutWorker(boolean deleteAccount) {
        LogoutWorker.attach(getFragmentManager(), deleteAccount);
    }

    @Override
    public void showLeaveGroupDialog(@StringRes int message) {
        ConfirmationDialogFragment.display(getFragmentManager(), getString(message),
                R.string.dialog_positive_leave);
    }

    @Override
    public void showDeleteAccountDialog() {
        DeleteAccountDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showProgressDialog(@StringRes int message) {
        mProgressDialog = ProgressDialog.show(getActivity(), null, getString(message), true);
    }

    @Override
    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void setResult(int result) {
        getActivity().setResult(result);
    }

    @Override
    public void finishScreen() {
        getActivity().finish();
    }

    @Override
    public boolean isNetworkAvailable() {
        return Utils.isNetworkAvailable(getActivity());
    }

    @Override
    public void showMessage(@StringRes int resId) {
        Snackbar.make(getView(), resId, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessage(@StringRes int resId, @NonNull String... args) {
        Snackbar.make(getView(), getString(resId, args), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action) {
        Snackbar.make(getView(), resId, Snackbar.LENGTH_LONG)
                .setAction(action.getActionText(), action)
                .show();
    }

    @Override
    public void removeWorker(@NonNull String workerTag) {
        WorkerUtils.removeWorker(getFragmentManager(), workerTag);
    }

    public interface ActivityListener {
        void setSettingsViewModel(@NonNull SettingsViewModel viewModel);
    }
}