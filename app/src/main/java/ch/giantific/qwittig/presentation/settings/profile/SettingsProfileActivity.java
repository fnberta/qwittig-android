/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.ActivitySettingsProfileBinding;
import ch.giantific.qwittig.presentation.common.BaseActivity;
import ch.giantific.qwittig.presentation.common.GoogleApiClientDelegate;
import ch.giantific.qwittig.presentation.common.Navigator;
import ch.giantific.qwittig.presentation.common.di.GoogleApiClientDelegateModule;
import ch.giantific.qwittig.presentation.common.di.NavigatorModule;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.EmailReAuthenticateDialogFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.FacebookUserWorkerListener;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorkerListener;
import ch.giantific.qwittig.presentation.settings.profile.di.DaggerSettingsProfileComponent;
import ch.giantific.qwittig.presentation.settings.profile.di.SettingsProfileComponent;
import ch.giantific.qwittig.presentation.settings.profile.di.SettingsProfileViewModelModule;
import ch.giantific.qwittig.utils.AvatarUtils;
import rx.Single;

/**
 * Hosts {@link SettingsProfileFragment} that allows to user to change his profile information.
 * <p/>
 * Shows the user's avatar as backdrop image in the toolbar with a parallax collapse animation on
 * scroll.
 * <p/>
 * Subclass of {@link BaseActivity}.
 * <p/>
 *
 * @see android.support.design.widget.CollapsingToolbarLayout
 */
public class SettingsProfileActivity extends BaseActivity<SettingsProfileComponent> implements
        SettingsProfileFragment.ActivityListener,
        DiscardChangesDialogFragment.DialogInteractionListener,
        GoogleUserWorkerListener, EmailUserWorkerListener, FacebookUserWorkerListener,
        EmailReAuthenticateDialogFragment.DialogInteractionListener,
        GoogleApiClientDelegate.GoogleLoginCallback {

    @Inject
    SettingsProfileViewModel mProfileViewModel;
    @Inject
    GoogleApiClientDelegate mGoogleApiDelegate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivitySettingsProfileBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_settings_profile);
        binding.setViewModel(mProfileViewModel);

        supportPostponeEnterTransition();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsProfileFragment())
                    .commit();
        }

        mGoogleApiDelegate.createGoogleApiClient();
    }

    @Override
    protected void injectDependencies(@Nullable Bundle savedInstanceState) {
        mComponent = DaggerSettingsProfileComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(this))
                .navigatorModule(new NavigatorModule(this))
                .googleApiClientDelegateModule(new GoogleApiClientDelegateModule(this, this, null))
                .settingsProfileViewModelModule(new SettingsProfileViewModelModule(savedInstanceState))
                .build();
        mComponent.inject(this);
    }

    @Override
    protected List<ViewModel> getViewModels() {
        return Arrays.asList(new ViewModel[]{mProfileViewModel});
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mProfileViewModel.onExitClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        mProfileViewModel.onExitClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mGoogleApiDelegate.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Navigator.INTENT_REQUEST_IMAGE_PICK:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri imageUri = data.getData();
                    AvatarUtils.saveImageLocal(this, imageUri, new AvatarUtils.AvatarLocalSaveListener() {
                        @Override
                        public void onAvatarSaved(@NonNull String path) {
                            mProfileViewModel.onNewAvatarTaken(path);
                        }
                    });
                }
        }
    }

    @Override
    public void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password) {
        mProfileViewModel.onValidEmailAndPasswordEntered(email, password);
    }

    @Override
    public void loginWithGoogle() {
        mGoogleApiDelegate.loginWithGoogle();
    }

    @Override
    public void onGoogleLoginSuccessful(@NonNull String idToken) {
        mProfileViewModel.onGoogleLoginSuccessful(idToken);
    }

    @Override
    public void onGoogleLoginFailed() {
        mProfileViewModel.onGoogleLoginFailed();
    }

    @Override
    public void setGoogleUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        mProfileViewModel.setGoogleUserStream(single, workerTag);
    }

    @Override
    public void setFacebookUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        mProfileViewModel.setFacebookUserStream(single, workerTag);
    }

    @Override
    public void setEmailUserStream(@NonNull Single<Void> single, @NonNull String workerTag) {
        mProfileViewModel.setEmailUserStream(single, workerTag);
    }

    @Override
    public void onDiscardChangesSelected() {
        mProfileViewModel.onDiscardChangesSelected();
    }
}
