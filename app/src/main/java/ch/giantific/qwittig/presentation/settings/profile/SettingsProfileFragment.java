/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsProfileBinding;
import ch.giantific.qwittig.presentation.settings.profile.di.DaggerSettingsProfileComponent;
import ch.giantific.qwittig.presentation.settings.profile.di.SettingsProfileViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.utils.Utils;
import timber.log.Timber;

/**
 * Displays the profile details of the current user, allowing him/her to edit them.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsProfileFragment extends BaseFragment<SettingsProfileViewModel, SettingsProfileFragment.ActivityListener>
        implements SettingsProfileViewModel.ViewListener {

    private static final int INTENT_REQUEST_IMAGE = 1;
    private static final int PERMISSIONS_REQUEST_EXT_STORAGE = 1;
    private Snackbar mSnackbar;

    private FragmentSettingsProfileBinding mBinding;

    public SettingsProfileFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        DaggerSettingsProfileComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .settingsProfileViewModelModule(new SettingsProfileViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSettingsProfileBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
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
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri imageUri = data.getData();
                    try {
                        final String avatarPath = AvatarUtils.copyAvatarLocal(getActivity(), imageUri);
                        mViewModel.onNewAvatarTaken(avatarPath);
                    } catch (IOException e) {
                        Timber.e(e, "Failed to pick profile image");
                    }
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_EXT_STORAGE:
                if (Utils.verifyPermissions(grantResults)) {
                    loadImagePicker();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setProfileViewModel(mViewModel);
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
    public void loadUnlinkThirdPartyWorker(@UnlinkThirdPartyWorker.ProfileAction int unlinkAction) {
        UnlinkThirdPartyWorker.attachUnlink(getFragmentManager(), unlinkAction);
    }

    @Override
    public void showDiscardChangesDialog() {
        DiscardChangesDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showAvatarPicker() {
        if (permissionsAreGranted()) {
            loadImagePicker();
        }
    }

    private boolean permissionsAreGranted() {
        final int readStorage = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (readStorage != PackageManager.PERMISSION_GRANTED) {
            final String[] permissionsArray = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissionsArray, PERMISSIONS_REQUEST_EXT_STORAGE);
            return false;
        }

        return true;
    }

    private void loadImagePicker() {
        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, INTENT_REQUEST_IMAGE);
    }

    @Override
    public void showSetPasswordMessage(@StringRes int message) {
        mSnackbar = Snackbar.make(getSnackbarView(), message, Snackbar.LENGTH_INDEFINITE);
        mSnackbar.show();
    }

    @Override
    public void dismissSetPasswordMessage() {
        mSnackbar.dismiss();
    }

    @Override
    public void reloadOptionsMenu() {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void finishScreen(int result) {
        final FragmentActivity activity = getActivity();
        activity.setResult(result);
        ActivityCompat.finishAfterTransition(activity);
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * <p/>
     * Extends {@link BaseFragment.ActivityListener}.
     */
    public interface ActivityListener extends BaseFragment.ActivityListener {

        /**
         * Sets the view model to the activity.
         *
         * @param viewModel the view model to set
         */
        void setProfileViewModel(@NonNull SettingsProfileViewModel viewModel);
    }
}
