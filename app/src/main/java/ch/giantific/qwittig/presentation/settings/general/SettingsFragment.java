/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general;


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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.MessageAction;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.EmailReAuthenticateDialogFragment;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorker;
import ch.giantific.qwittig.presentation.common.workers.FacebookUserWorker;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorker;
import ch.giantific.qwittig.presentation.settings.general.di.SettingsComponent;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupActivity;
import ch.giantific.qwittig.presentation.settings.groupusers.users.SettingsUsersActivity;
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
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        SettingsViewModel.ViewListener {

    private static final String PREF_PROFILE = "PREF_PROFILE";
    private static final String PREF_GROUP_CURRENT = "PREF_GROUP_CURRENT";
    private static final String PREF_GROUP_ADD_NEW = "PREF_GROUP_ADD_NEW";
    private static final String PREF_CATEGORY_CURRENT_GROUP = "PREF_CATEGORY_CURRENT_GROUP";
    private static final String PREF_GROUP_NAME = "PREF_GROUP_NAME";
    private static final String PREF_GROUP_LEAVE = "PREF_GROUP_LEAVE";
    private static final String PREF_GROUP_USERS = "PREF_GROUP_USERS";

    @Inject
    SettingsViewModel settingsViewModel;
    @Inject
    SharedPreferences sharedPrefs;
    private ActivityListener activity;
    private PreferenceCategory categoryCurrentGroup;
    private ListPreference listPreferenceGroupCurrent;
    private EditTextPreference etPreferenceGroupName;
    private Preference preferenceGroupLeave;
    private ProgressDialog progressDialog;
    private CallbackManager facebookCallbackManager;

    public SettingsFragment() {
        // required empty constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activity = (ActivityListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ActivityListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                settingsViewModel.onFacebookSignedIn(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                settingsViewModel.onFacebookLoginFailed();
            }

            @Override
            public void onError(FacebookException exception) {
                settingsViewModel.onFacebookLoginFailed();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        final FragmentActivity activity = getActivity();
        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false);
        addPreferencesFromResource(R.xml.preferences);

        categoryCurrentGroup = (PreferenceCategory)
                findPreference(PREF_CATEGORY_CURRENT_GROUP);
        listPreferenceGroupCurrent = (ListPreference)
                findPreference(PREF_GROUP_CURRENT);
        etPreferenceGroupName = (EditTextPreference)
                findPreference(PREF_GROUP_NAME);
        preferenceGroupLeave = findPreference(PREF_GROUP_LEAVE);
        preferenceGroupLeave.setOnPreferenceClickListener(preference -> {
            settingsViewModel.onLeaveGroupClick();
            return true;
        });

        final Preference prefProfile = findPreference(PREF_PROFILE);
        prefProfile.setOnPreferenceClickListener(preference -> {
            final Intent intent = new Intent(activity, SettingsProfileActivity.class);
            final ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
            activity.startActivityForResult(intent, Navigator.INTENT_REQUEST_SETTINGS_PROFILE,
                    activityOptionsCompat.toBundle());
            return true;
        });
        final Preference prefGroupNew = findPreference(PREF_GROUP_ADD_NEW);
        prefGroupNew.setOnPreferenceClickListener(preference -> {
            final Intent intent = new Intent(activity, SettingsAddGroupActivity.class);
            final ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
            activity.startActivityForResult(intent, Navigator.INTENT_REQUEST_SETTINGS_ADD_GROUP,
                    activityOptionsCompat.toBundle());
            return true;
        });
        final Preference prefGroupAddUser = findPreference(PREF_GROUP_USERS);
        prefGroupAddUser.setOnPreferenceClickListener(preference -> {
            final Intent intent = new Intent(activity, SettingsUsersActivity.class);
            final ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
            activity.startActivity(intent, activityOptionsCompat.toBundle());
            return true;
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        settingsViewModel.saveState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity.getComponent().inject(this);
        settingsViewModel.attachView(this);
        settingsViewModel.onPreferencesLoaded();
    }

    @Override
    public void onStart() {
        super.onStart();

        settingsViewModel.onViewVisible();
    }

    @Override
    public void onResume() {
        super.onResume();

        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @NonNull String key) {
        switch (key) {
            case PREF_GROUP_CURRENT:
                final String groupId = sharedPrefs.getString(key, "");
                settingsViewModel.onGroupSelected(groupId);
                break;
            case PREF_GROUP_NAME:
                final String name = sharedPrefs.getString(key, "");
                settingsViewModel.onGroupNameChanged(name);
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
                settingsViewModel.onLogoutMenuClick();
                return true;
            case R.id.action_account_delete:
                settingsViewModel.onDeleteAccountMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        settingsViewModel.onViewGone();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setupGroupSelection(@NonNull CharSequence[] entries, @NonNull CharSequence[] values,
                                    @NonNull String selectedValue) {
        listPreferenceGroupCurrent.setEntries(entries);
        listPreferenceGroupCurrent.setEntryValues(values);
        listPreferenceGroupCurrent.setValue(selectedValue);
        setupListPreference(listPreferenceGroupCurrent, selectedValue);
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
        pref.setOnPreferenceChangeListener((preference, o) -> {
            int index = pref.findIndexOfValue(o.toString());

            if (index >= 0) {
                preference.setSummary(pref.getEntries()[index]);
            } else {
                preference.setSummary(null);
            }
            return true;
        });
    }

    @Override
    public void setCurrentGroupTitle(@NonNull String title) {
        categoryCurrentGroup.setTitle(title);
    }

    @Override
    public void setChangeGroupNameText(@NonNull String text) {
        etPreferenceGroupName.setText(text);
    }

    @Override
    public void setLeaveGroupTitle(@StringRes int message, @NonNull String groupName) {
        preferenceGroupLeave.setTitle(getString(message, groupName));
    }

    @Override
    public void loadGoogleUserWorker() {
        GoogleUserWorker.attachSignOut(getFragmentManager());
    }

    @Override
    public void loadDeleteGoogleUserWorker(@NonNull String idToken) {
        GoogleUserWorker.attachDelete(getFragmentManager(), idToken);
    }

    @Override
    public void loadDeleteFacebookUserWorker(@NonNull String token) {
        FacebookUserWorker.attachDelete(getFragmentManager(), token);
    }

    @Override
    public void loadDeleteEmailUserWorker(@NonNull String currentEmail,
                                          @NonNull String currentPassword) {
        EmailUserWorker.attachDelete(getFragmentManager(), currentEmail, currentPassword);
    }

    @Override
    public void showLeaveGroupDialog(@StringRes int message) {
        LeaveGroupDialogFragment.display(getFragmentManager(), message);
    }

    @Override
    public void showDeleteAccountDialog() {
        DeleteAccountDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showProgressDialog(@StringRes int message) {
        progressDialog = ProgressDialog.show(getActivity(), null, getString(message), true);
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void reAuthenticateGoogle() {
        activity.loginWithGoogle();
    }

    @Override
    public void reAuthenticateFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList("email", "public_profile"));
    }

    @Override
    public void showEmailReAuthenticateDialog(@Nullable String email) {
        EmailReAuthenticateDialogFragment.display(getFragmentManager(),
                R.string.dialog_account_delete_message_email, email);
    }

    @Override
    public boolean isNetworkAvailable() {
        return Utils.isNetworkAvailable(getActivity());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void showMessage(@StringRes int resId) {
        Snackbar.make(getView(), resId, Snackbar.LENGTH_LONG).show();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void showMessage(@StringRes int resId, @NonNull Object... args) {
        Snackbar.make(getView(), getString(resId, args), Snackbar.LENGTH_LONG).show();
    }

    @SuppressWarnings("ConstantConditions")
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

    public interface ActivityListener extends BaseFragment.ActivityListener<SettingsComponent> {
        void loginWithGoogle();
    }
}