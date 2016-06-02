/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginProfileBinding;
import ch.giantific.qwittig.domain.repositories.UserRepository;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.login.di.DaggerLoginProfileComponent;
import ch.giantific.qwittig.presentation.login.di.LoginProfileViewModelModule;
import ch.giantific.qwittig.utils.Utils;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginProfileFragment extends BaseFragment<LoginProfileViewModel, BaseFragment.ActivityListener>
        implements LoginProfileViewModel.ViewListener {

    private static final int INTENT_REQUEST_IMAGE = 1;
    private FragmentLoginProfileBinding mBinding;

    public LoginProfileFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerLoginProfileComponent.builder()
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
                    mViewModel.onNewAvatarTaken(imageUri.toString());
                }
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
        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, INTENT_REQUEST_IMAGE);
    }

    @Override
    public Single<byte[]> encodeAvatar(@NonNull final String avatar) {
        final LoginProfileFragment frag = this;
        return Single.create(new Single.OnSubscribe<byte[]>() {
            @Override
            public void call(final SingleSubscriber<? super byte[]> singleSubscriber) {
                Glide.with(frag)
                        .load(avatar)
                        .asBitmap()
                        .toBytes(Bitmap.CompressFormat.JPEG, UserRepository.JPEG_COMPRESSION_RATE)
                        .centerCrop()
                        .into(new SimpleTarget<byte[]>(UserRepository.WIDTH, UserRepository.HEIGHT) {
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
}
