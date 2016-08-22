/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.EmailReAuthenticateDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.FacebookUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorkerListener;
import ch.giantific.qwittig.presentation.settings.general.di.DaggerSettingsComponent;
import ch.giantific.qwittig.presentation.settings.general.di.SettingsComponent;
import ch.giantific.qwittig.presentation.settings.general.di.SettingsViewModelModule;
import ch.giantific.qwittig.presentation.settings.groupusers.addgroup.SettingsAddGroupFragment;
import ch.giantific.qwittig.presentation.settings.profile.SettingsProfileViewModel;
import rx.Single;

/**
 * Hosts {@link SettingsFragment} containing the main settings options.
 * <p>
 * Subclass of {@link BaseActivity}.
 */
public class SettingsActivity extends BaseActivity<SettingsComponent> implements
        SettingsFragment.ActivityListener,
        LeaveGroupDialogFragment.DialogInteractionListener,
        DeleteAccountDialogFragment.DialogInteractionListener,
        EmailUserWorkerListener, GoogleUserWorkerListener, FacebookUserWorkerListener,
        EmailReAuthenticateDialogFragment.DialogInteractionListener,
        GoogleApiClientDelegate.GoogleLoginCallback {

    @Inject
    SettingsViewModel settingsViewModel;
    @Inject
    GoogleApiClientDelegate googleApiDelegate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // set default Result to OK, if logout is triggered it will be set to LOGOUT in order to
        // finish HomeActivity as well
        setResult(RESULT_OK);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }

        googleApiDelegate.createGoogleApiClient();
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        component = DaggerSettingsComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .googleApiClientDelegateModule(new GoogleApiClientDelegateModule(this, this, null))
                .settingsViewModelModule(new SettingsViewModelModule(savedInstanceState))
                .build();
        component.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{settingsViewModel});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleApiDelegate.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_SETTINGS_PROFILE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        showMessage(R.string.toast_profile_update);
                        break;
                    case SettingsProfileViewModel.Result.CHANGES_DISCARDED:
                        showMessage(R.string.toast_changes_discarded);
                        break;
                }
                break;
            case Navigator.INTENT_REQUEST_SETTINGS_ADD_GROUP:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        final String newGroupName =
                                data.getStringExtra(SettingsAddGroupFragment.RESULT_DATA_GROUP);
                        showMessage(R.string.toast_group_added, newGroupName);
                        break;
                }
                break;
        }
    }

    @Override
    public void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password) {
        settingsViewModel.onValidEmailAndPasswordEntered(email, password);
    }

    @Override
    public void onLeaveGroupSelected() {
        settingsViewModel.onLeaveGroupSelected();
    }

    @Override
    public void loginWithGoogle() {
        googleApiDelegate.loginWithGoogle();
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        settingsViewModel.onGoogleLoginSuccessful(idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        settingsViewModel.onGoogleLoginFailed();
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        settingsViewModel.setEmailUserStream(single, workerTag);
    }

    @Override
    public void setGoogleUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        settingsViewModel.setGoogleUserStream(single, workerTag);
    }

    @Override
    public void setFacebookUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        settingsViewModel.setFacebookUserStream(single, workerTag);
    }

    @Override
    public void onDeleteAccountSelected() {
        settingsViewModel.onDeleteAccountSelected();
    }
}
