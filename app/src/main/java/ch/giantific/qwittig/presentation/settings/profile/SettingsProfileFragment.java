/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentSettingsProfileBinding;
import ch.giantific.qwittig.di.components.DaggerSettingsProfileComponent;
import ch.giantific.qwittig.di.modules.SettingsProfileViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.AvatarUtils;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Displays the profile details of the current user, allowing him/her to edit them.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsProfileFragment extends BaseFragment<SettingsProfileViewModel, SettingsProfileFragment.ActivityListener>
        implements SettingsProfileViewModel.ViewListener {

    private static final int INTENT_REQUEST_IMAGE = 1;
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
                    mViewModel.onNewAvatarTaken(imageUri.toString());
                }
        }
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setProfileViewModel(mViewModel);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.etNickname;
    }

    @Override
    public void startPostponedEnterTransition() {
        ActivityCompat.startPostponedEnterTransition(getActivity());
    }

    @Override
    public void loadSaveAvatarWorker(@NonNull String nickname, @NonNull byte[] avatar) {
        SettingsProfileWorker.attachSaveAvatar(getFragmentManager(), nickname, avatar);
    }

    @Override
    public void loadUnlinkThirdPartyWorker(@SettingsProfileWorker.ProfileAction int unlinkAction) {
        SettingsProfileWorker.attachUnlink(getFragmentManager(), unlinkAction);
    }

    @Override
    public void showDiscardChangesDialog() {
        DiscardChangesDialogFragment.display(getFragmentManager());
    }

    @Override
    public void showAvatarPicker() {
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
    public Single<byte[]> encodeAvatar(@NonNull final String avatar) {
        final SettingsProfileFragment frag = this;
        return Single.create(new Single.OnSubscribe<byte[]>() {
            @Override
            public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                Glide.with(frag)
                        .load(avatar)
                        .asBitmap()
                        .toBytes(Bitmap.CompressFormat.JPEG, AvatarUtils.JPEG_COMPRESSION_RATE)
                        .centerCrop()
                        .into(new SimpleTarget<byte[]>(AvatarUtils.WIDTH, AvatarUtils.HEIGHT) {
                            @Override
                            public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                                if (!singleSubscriber.isUnsubscribed()) {
                                    singleSubscriber.onSuccess(resource);
                                }
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                super.onLoadFailed(e, errorDrawable);

                                if (!singleSubscriber.isUnsubscribed()) {
                                    singleSubscriber.onError(e);
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void reloadOptionsMenu() {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void startSaveAnim() {
        mActivity.startProgressAnim();
    }

    @Override
    public void stopSaveAnim() {
        mActivity.stopProgressAnim();
    }

    @Override
    public void showSaveFinishedAnim() {
        mActivity.startFinalProgressAnim();
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

        /**
         * Indicates to start the loading animation of the {@link FabProgress}.
         */
        void startProgressAnim();

        /**
         * Indicates to start the final loading animation of the {@link FabProgress}.
         */
        void startFinalProgressAnim();

        /**
         * Indicates to hide the loading animation of the {@link FabProgress}.
         */
        void stopProgressAnim();
    }
}
