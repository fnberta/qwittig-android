/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsProfileBinding;
import ch.giantific.qwittig.presentation.common.BaseFragment;
import ch.giantific.qwittig.presentation.common.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.dialogs.EmailReAuthenticateDialogFragment;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorker;
import ch.giantific.qwittig.presentation.common.workers.FacebookUserWorker;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorker;
import ch.giantific.qwittig.presentation.settings.profile.di.SettingsProfileComponent;

/**
 * Displays the profile details of the current user, allowing him/her to edit them.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsProfileFragment extends BaseFragment<SettingsProfileComponent, SettingsProfileContract.Presenter, SettingsProfileFragment.ActivityListener> implements
        SettingsProfileContract.ViewListener,
        EmailReAuthenticateDialogFragment.DialogInteractionListener {

    private FragmentSettingsProfileBinding binding;
    private Snackbar snackbarSetPassword;
    private ProgressDialog progressDialog;
    private CallbackManager facebookCallbackManager;

    public SettingsProfileFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                presenter.onFacebookSignedIn(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                presenter.onFacebookLoginFailed();
            }

            @Override
            public void onError(FacebookException exception) {
                presenter.onFacebookLoginFailed();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        presenter.attachView(this);
        binding.setPresenter(presenter);
        binding.setViewModel(presenter.getViewModel());
    }

    @Override
    protected void injectDependencies(@NonNull SettingsProfileComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings_profile, menu);

        final MenuItem deleteAvatar = menu.findItem(R.id.action_settings_profile_avatar_delete);
        deleteAvatar.setVisible(presenter.showDeleteAvatar());

        final MenuItem unlinkFacebook = menu.findItem(R.id.action_settings_profile_unlink_facebook);
        unlinkFacebook.setVisible(presenter.showUnlinkFacebook());

        final MenuItem unlinkGoogle = menu.findItem(R.id.action_settings_profile_unlink_google);
        unlinkGoogle.setVisible(presenter.showUnlinkGoogle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings_profile_avatar_edit:
                presenter.onPickAvatarMenuClick();
                return true;
            case R.id.action_settings_profile_avatar_delete:
                presenter.onDeleteAvatarMenuClick();
                return true;
            case R.id.action_settings_profile_unlink_facebook:
                // fall through
            case R.id.action_settings_profile_unlink_google:
                presenter.onUnlinkThirdPartyLoginMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected View getSnackbarView() {
        return binding.etSettingsProfileNickname;
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
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
    public void loadUnlinkGoogleWorker(@NonNull String email, @NonNull String password,
                                       @NonNull String idToken) {
        GoogleUserWorker.attachUnlink(getFragmentManager(), email, password, idToken);
    }

    @Override
    public void loadUnlinkFacebookWorker(@NonNull String email, @NonNull String password,
                                         @NonNull String token) {
        FacebookUserWorker.attachUnlink(getFragmentManager(), email, password, token);
    }

    @Override
    public void showDiscardChangesDialog() {
        DiscardChangesDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showSetPasswordMessage(@StringRes int message) {
        snackbarSetPassword = Snackbar.make(getSnackbarView(), message, Snackbar.LENGTH_INDEFINITE);
        snackbarSetPassword.show();
    }

    @Override
    public void dismissSetPasswordMessage() {
        snackbarSetPassword.dismiss();
    }

    @Override
    public void reloadOptionsMenu() {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void showReAuthenticateDialog(@NonNull String currentEmail) {
        EmailReAuthenticateDialogFragment.display(getFragmentManager(),
                R.string.dialog_reauthenticate_message, currentEmail);
    }

    @Override
    public void onValidEmailAndPasswordEntered(@NonNull String email, @NonNull String password) {
        presenter.onValidEmailAndPasswordEntered(email, password);
    }

    @Override
    public void loadChangeEmailPasswordWorker(@NonNull String currentEmail,
                                              @NonNull String currentPassword,
                                              @Nullable String newEmail,
                                              @Nullable String newPassword) {
        EmailUserWorker.attachChangeEmailPasswordInstance(getFragmentManager(),
                currentEmail, currentPassword, newEmail, newPassword);
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

    public interface ActivityListener extends BaseFragment.ActivityListener<SettingsProfileComponent> {
        void loginWithGoogle();
    }
}
