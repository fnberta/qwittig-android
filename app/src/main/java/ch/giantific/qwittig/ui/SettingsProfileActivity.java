package ch.giantific.qwittig.ui;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;
import android.transition.Explode;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.helper.AvatarHelper;
import ch.giantific.qwittig.data.models.ImageAvatar;
import ch.giantific.qwittig.ui.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.Utils;

public class SettingsProfileActivity extends BaseActivity implements
        SettingsProfileFragment.FragmentInteractionListener,
        AvatarHelper.HelperInteractionListener,
        DiscardChangesDialogFragment.DialogInteractionListener {

    @IntDef({CHANGES_SAVED, CHANGES_DISCARDED, NO_CHANGES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EditAction {}
    public static final int CHANGES_SAVED = 0;
    public static final int CHANGES_DISCARDED = 1;
    public static final int NO_CHANGES = 2;

    public static final int RESULT_CHANGES_DISCARDED = 2;
    private static final String PROFILE_FRAGMENT = "profile_fragment";
    private static final String ASYNC_AVATAR_FRAGMENT = "async_avatar_fragment";
    private static final int INTENT_REQUEST_IMAGE = 1;
    private SettingsProfileFragment mSettingsProfileFragment;
    private byte[] mAvatar;

    @Override
    public byte[] getAvatar() {
        return mAvatar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);

        if (Utils.isRunningLollipopAndHigher()) {
            setActivityTransition();
        }

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsProfileFragment(), PROFILE_FRAGMENT)
                    .commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setActivityTransition() {
        Transition transitionEnter = new Explode();
        transitionEnter.excludeTarget(android.R.id.statusBarBackground, true);
        transitionEnter.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(transitionEnter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSettingsProfileFragment = (SettingsProfileFragment)
                getFragmentManager().findFragmentByTag(PROFILE_FRAGMENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                checkForChangesAndExit();
                return true;
            case R.id.action_settings_profile_save:
                mSettingsProfileFragment.saveChanges();
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
        discardChangesDialogFragment.show(getFragmentManager(), "discard_changes");
    }

    @Override
    public void discardChanges() {
        finishEdit(CHANGES_DISCARDED);
    }

    @Override
    public void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, INTENT_REQUEST_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE:
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = data.getData();
                    getAvatarAsync(imageUri);
                }
        }
    }

    private void getAvatarAsync(Uri imageUri) {
        FragmentManager fragmentManager = getFragmentManager();
        AvatarHelper avatarHelper = findAvatarHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (avatarHelper == null) {
            avatarHelper = AvatarHelper.newInstance(imageUri);

            fragmentManager.beginTransaction()
                    .add(avatarHelper, ASYNC_AVATAR_FRAGMENT)
                    .commit();
        }
    }

    private AvatarHelper findAvatarHelper(FragmentManager fragmentManager) {
        return (AvatarHelper)
                    fragmentManager.findFragmentByTag(ASYNC_AVATAR_FRAGMENT);
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
    public void onPostExecute(ImageAvatar avatar) {
        if (avatar != null) {
            mAvatar = avatar.getByteArray();
            mSettingsProfileFragment.setAvatarImage(avatar);
        }

        // remote async fragment because we need a new one when the user clicks on the avatar again
        FragmentManager fragmentManager = getFragmentManager();
        AvatarHelper avatarHelper = findAvatarHelper(fragmentManager);
        if (avatarHelper != null) {
            fragmentManager.beginTransaction().remove(avatarHelper).commit();
        }
    }

    @Override
    public void onBackPressed() {
        checkForChangesAndExit();
    }
}
