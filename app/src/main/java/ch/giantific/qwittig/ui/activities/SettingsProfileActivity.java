/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Avatar;
import ch.giantific.qwittig.data.parse.models.User;
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
        DiscardChangesDialogFragment.DialogInteractionListener {

    @IntDef({CHANGES_SAVED, CHANGES_DISCARDED, NO_CHANGES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EditAction {}
    public static final int CHANGES_SAVED = 0;
    public static final int CHANGES_DISCARDED = 1;
    public static final int NO_CHANGES = 2;
    public static final int RESULT_CHANGES_DISCARDED = 2;
    private static final String STATE_PROFILE_FRAGMENT = "STATE_PROFILE_FRAGMENT";
    private static final String DISCARD_CHANGES_DIALOG = "DISCARD_CHANGES_DIALOG";
    private static final int INTENT_REQUEST_IMAGE = 1;
    private boolean mHasAvatarSet = true;
    private ImageView mImageViewAvatar;
    private SettingsProfileFragment mSettingsProfileFragment;

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
        setAvatar();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsProfileFragment.saveChanges();
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

    private void setAvatar() {
        User currentUser = (User) ParseUser.getCurrentUser();

        byte[] avatarByteArray = currentUser.getAvatar();
        if (avatarByteArray != null) {
            Glide.with(this)
                    .load(avatarByteArray)
                    .asBitmap()
                    .into(new BitmapImageViewTarget(mImageViewAvatar) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            view.setImageBitmap(resource);
                            supportStartPostponedEnterTransition();
                        }
                    });
        } else {
            mHasAvatarSet = false;
            invalidateOptionsMenu();

            mImageViewAvatar.setImageDrawable(Avatar.getFallbackDrawableRect(this, false));
            supportStartPostponedEnterTransition();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getFragmentManager().putFragment(outState, STATE_PROFILE_FRAGMENT, mSettingsProfileFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings_profile, menu);
        MenuItem deleteAvatar = menu.findItem(R.id.action_settings_profile_avatar_delete);
        deleteAvatar.setVisible(mHasAvatarSet);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                checkForChangesAndExit();
                return true;
            case R.id.action_settings_profile_avatar_edit:
                pickAvatar();
                return true;
            case R.id.action_settings_profile_avatar_delete:
                deleteAvatar();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkForChangesAndExit() {
        if (mSettingsProfileFragment.changesWereMade()) {
            showDiscardChangesDialog();
        } else {
            finishEdit(NO_CHANGES);
        }
    }

    private void showDiscardChangesDialog() {
        DiscardChangesDialogFragment discardChangesDialogFragment =
                new DiscardChangesDialogFragment();
        discardChangesDialogFragment.show(getFragmentManager(), DISCARD_CHANGES_DIALOG);
    }

    @Override
    public void onDiscardChangesSelected() {
        finishEdit(CHANGES_DISCARDED);
    }

    @Override
    public void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, INTENT_REQUEST_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    mSettingsProfileFragment.setAvatar(imageUri);
                    mHasAvatarSet = true;
                    invalidateOptionsMenu();

                    Glide.with(this)
                            .load(imageUri)
                            .into(mImageViewAvatar);
                }
        }
    }

    private void deleteAvatar() {
        mSettingsProfileFragment.deleteAvatar();
        mImageViewAvatar.setImageDrawable(Avatar.getFallbackDrawableRect(this, false));
        mHasAvatarSet = false;
        invalidateOptionsMenu();
    }

    @Override
    public void finishEdit(@EditAction int editAction) {
        switch (editAction) {
            case CHANGES_SAVED:
                setResult(RESULT_OK);
                break;
            case CHANGES_DISCARDED:
                setResult(RESULT_CHANGES_DISCARDED);
                break;
            case NO_CHANGES:
                setResult(RESULT_CANCELED);
        }

        ActivityCompat.finishAfterTransition(this);
    }

    @Override
    public void onBackPressed() {
        checkForChangesAndExit();
    }
}
