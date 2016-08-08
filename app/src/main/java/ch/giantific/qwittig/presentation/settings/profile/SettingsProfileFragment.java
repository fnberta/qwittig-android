/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsProfileBinding;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.presentation.common.fragments.dialogs.EmailReAuthenticateDialogFragment;
import ch.giantific.qwittig.presentation.common.workers.EmailUserWorker;
import ch.giantific.qwittig.presentation.common.workers.GoogleUserWorker;
import ch.giantific.qwittig.presentation.settings.profile.di.SettingsProfileComponent;

/**
 * Displays the profile details of the current user, allowing him/her to edit them.
 * <p>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsProfileFragment extends BaseFragment<SettingsProfileComponent, SettingsProfileViewModel, SettingsProfileFragment.ActivityListener> implements
        SettingsProfileViewModel.ViewListener,
        EmailReAuthenticateDialogFragment.DialogInteractionListener {

    private FragmentSettingsProfileBinding mBinding;
    private Snackbar mSnackbarSetPassword;
    private ProgressDialog mProgressDialog;

    public SettingsProfileFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSettingsProfileBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.attachView(this);
        mBinding.setViewModel(mViewModel);
    }

    @Override
    protected void injectDependencies(@NonNull SettingsProfileComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings_profile, menu);

        final MenuItem deleteAvatar = menu.findItem(R.id.action_settings_profile_avatar_delete);
        deleteAvatar.setVisible(!TextUtils.isEmpty(mViewModel.getAvatar()));

        final MenuItem unlinkFacebook = menu.findItem(R.id.action_settings_profile_unlink_facebook);
        unlinkFacebook.setVisible(mViewModel.showUnlinkFacebook());

        final MenuItem unlinkGoogle = menu.findItem(R.id.action_settings_profile_unlink_google);
        unlinkGoogle.setVisible(mViewModel.showUnlinkGoogle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings_profile_avatar_edit:
                mViewModel.onPickAvatarMenuClick();
                return true;
            case R.id.action_settings_profile_avatar_delete:
                mViewModel.onDeleteAvatarMenuClick();
                return true;
            case R.id.action_settings_profile_unlink_facebook:
                // fall through
            case R.id.action_settings_profile_unlink_google:
                mViewModel.onUnlinkThirdPartyLoginMenuClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.etSettingsProfileNickname;
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
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
    public void loadUnlinkGoogleWorker(@NonNull String email, @NonNull String password,
                                       @NonNull String idToken) {
        GoogleUserWorker.attachUnlink(getFragmentManager(), email, password, idToken);
    }

    @Override
    public void loadUnlinkFacebookWorker() {
        // TODO: implement facebook un-linking
    }

    @Override
    public void showDiscardChangesDialog() {
        DiscardChangesDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showSetPasswordMessage(@StringRes int message) {
        mSnackbarSetPassword = Snackbar.make(getSnackbarView(), message, Snackbar.LENGTH_INDEFINITE);
        mSnackbarSetPassword.show();
    }

    @Override
    public void dismissSetPasswordMessage() {
        mSnackbarSetPassword.dismiss();
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
        mViewModel.onValidEmailAndPasswordEntered(email, password);
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
        mActivity.loginWithGoogle();
    }

    public interface ActivityListener extends BaseFragment.ActivityListener<SettingsProfileComponent> {
        void loginWithGoogle();
    }
}
