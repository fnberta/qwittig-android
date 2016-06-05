/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.IOException;

import ch.giantific.qwittig.Qwittig;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginProfileBinding;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.login.di.DaggerLoginProfileComponent;
import ch.giantific.qwittig.presentation.login.di.LoginProfileViewModelModule;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;
import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginProfileFragment extends BaseFragment<LoginProfileViewModel, BaseFragment.ActivityListener>
        implements LoginProfileViewModel.ViewListener {

    private static final int INTENT_REQUEST_IMAGE = 1;
    private static final int PERMISSIONS_REQUEST_EXT_STORAGE = 1;
    private FragmentLoginProfileBinding mBinding;

    public LoginProfileFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerLoginProfileComponent.builder()
                .applicationComponent(Qwittig.getAppComponent(getActivity()))
                .loginProfileViewModelModule(new LoginProfileViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentLoginProfileBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    final Uri imageUri = data.getData();
                    try {
                        final String avatarPath = AvatarUtils.copyAvatarLocal(getActivity(),
                                imageUri);
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
        // nothing to set
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.ivLoginProfileAvatar;
    }

    @Override
    public void finishScreen(int result) {
        final FragmentActivity activity = getActivity();
        activity.setResult(result);
        ActivityCompat.finishAfterTransition(activity);
    }

    @Override
    public void showFirstGroupFragment() {
        final LoginFirstGroupFragment fragment = new LoginFirstGroupFragment();
        if (Utils.isRunningLollipopAndHigher()) {
            setExitTransition(new Slide(Gravity.START));
            fragment.setEnterTransition(new Slide(Gravity.END));
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, LoginActivity.FRAGMENT_LOGIN)
                .addToBackStack(null)
                .commit();
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
}
