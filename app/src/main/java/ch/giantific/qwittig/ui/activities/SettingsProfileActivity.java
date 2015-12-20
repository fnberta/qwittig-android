/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.berta.fabio.fabprogress.ProgressFinalAnimationListener;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.workerfragments.account.UnlinkThirdPartyWorker;
import ch.giantific.qwittig.ui.fragments.SettingsProfileFragment;
import ch.giantific.qwittig.ui.fragments.dialogs.DiscardChangesDialogFragment;

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
public class SettingsProfileActivity extends BaseActivity implements
        SettingsProfileFragment.FragmentInteractionListener,
        DiscardChangesDialogFragment.DialogInteractionListener,
        UnlinkThirdPartyWorker.WorkerInteractionListener {

    private static final String STATE_PROFILE_FRAGMENT = "STATE_PROFILE_FRAGMENT";
    private static final String LOG_TAG = SettingsProfileActivity.class.getSimpleName();
    private ImageView mImageViewAvatar;
    private SettingsProfileFragment mSettingsProfileFragment;
    private FabProgress mFabProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);
        supportPostponeEnterTransition();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        mImageViewAvatar = (ImageView) findViewById(R.id.iv_avatar);

        mFabProgress = (FabProgress) findViewById(R.id.fab_save);
        mFabProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsProfileFragment.saveChanges();
            }
        });
        mFabProgress.setProgressFinalAnimationListener(new ProgressFinalAnimationListener() {
            @Override
            public void onProgressFinalAnimationComplete() {
                mSettingsProfileFragment.finishEdit(RESULT_OK);
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        if (savedInstanceState == null) {
            mSettingsProfileFragment = new SettingsProfileFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, mSettingsProfileFragment)
                    .commit();
        } else {
            mSettingsProfileFragment = (SettingsProfileFragment) fragmentManager
                    .getFragment(savedInstanceState, STATE_PROFILE_FRAGMENT);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getFragmentManager().putFragment(outState, STATE_PROFILE_FRAGMENT, mSettingsProfileFragment);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mSettingsProfileFragment.checkForChangesAndExit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setBackdropAvatar(byte[] avatar) {
        if (avatar != null) {
            Glide.with(this)
                    .load(avatar)
                    .asBitmap()
                    .into(new BitmapImageViewTarget(mImageViewAvatar) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            view.setImageBitmap(resource);
                            supportStartPostponedEnterTransition();
                        }
                    });
        } else {
            mImageViewAvatar.setImageDrawable(AvatarUtils.getFallbackDrawableRect(this, false));
            supportStartPostponedEnterTransition();
        }
    }

    @Override
    public void setBackdropAvatarFromUri(Uri avatar) {
        Glide.with(this)
                .load(avatar)
                .into(mImageViewAvatar);
    }

    @Override
    public void startProgressAnim() {
        mFabProgress.startProgress();
    }

    @Override
    public void onDiscardChangesSelected() {
        mSettingsProfileFragment.finishEdit(SettingsProfileFragment.RESULT_CHANGES_DISCARDED);
    }

    @Override
    public void onThirdPartyUnlinked() {
        mFabProgress.startProgressFinalAnimation();
    }

    @Override
    public void onThirdPartyUnlinkFailed(@StringRes int errorMessage) {
        mSettingsProfileFragment.onThirdPartyUnlinkFailed(errorMessage);
    }

    @Override
    public void onBackPressed() {
        mSettingsProfileFragment.checkForChangesAndExit();
    }
}
