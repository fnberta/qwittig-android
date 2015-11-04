/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.ParseUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Avatar;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.ui.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the profile details of the current user, allowing him/her to edit them.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsProfileFragment extends BaseFragment {

    @IntDef({CHANGES_SAVED, CHANGES_DISCARDED, NO_CHANGES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EditAction {}
    public static final int CHANGES_SAVED = 0;
    public static final int CHANGES_DISCARDED = 1;
    public static final int NO_CHANGES = 2;
    public static final int RESULT_CHANGES_DISCARDED = 2;
    private static final String DISCARD_CHANGES_DIALOG = "DISCARD_CHANGES_DIALOG";
    private static final int INTENT_REQUEST_IMAGE = 1;
    private FragmentInteractionListener mListener;
    private TextInputLayout mTextInputLayoutEmail;
    private EditText mEditTextEmail;
    private TextInputLayout mTextInputLayoutNickname;
    private EditText mEditTextNickname;
    private TextInputLayout mTextInputLayoutPassword;
    private EditText mEditTextPassword;
    private TextInputLayout mTextInputLayoutPasswordRepeat;
    private EditText mEditTextPasswordRepeat;
    private String mCurrentEmail;
    private String mCurrentNickname;
    private String mEmail;
    private String mNickname;
    private String mPassword;
    private String mPasswordRepeat;
    private byte[] mAvatar;
    private boolean mDeleteAvatar;
    private boolean mHasAvatarSet = true;
    public SettingsProfileFragment() {
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateCurrentUserAndGroup();
        mCurrentEmail = mCurrentUser.getUsername();
        mCurrentNickname = mCurrentUser.getNickname();

        mTextInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.til_email);
        mEditTextEmail = mTextInputLayoutEmail.getEditText();
        if (!ParseUtils.isTestUser(mCurrentUser)) {
            mEditTextEmail.setText(mCurrentEmail);
        }

        mTextInputLayoutNickname = (TextInputLayout) view.findViewById(R.id.til_nickname);
        mEditTextNickname = mTextInputLayoutNickname.getEditText();
        mEditTextNickname.setText(mCurrentNickname);

        mTextInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.til_password);
        mEditTextPassword = mTextInputLayoutPassword.getEditText();

        mTextInputLayoutPasswordRepeat = (TextInputLayout) view.findViewById(R.id.til_password_repeat);
        mEditTextPasswordRepeat = mTextInputLayoutPasswordRepeat.getEditText();

        checkAvatar();
    }

    private void checkAvatar() {
        byte[] avatar = mCurrentUser.getAvatar();
        mHasAvatarSet = avatar != null;
        getActivity().invalidateOptionsMenu();
        mListener.setBackdropAvatar(avatar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings_profile, menu);
        MenuItem deleteAvatar = menu.findItem(R.id.action_settings_profile_avatar_delete);
        deleteAvatar.setVisible(mHasAvatarSet);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings_profile_avatar_edit:
                pickAvatar();
                return true;
            case R.id.action_settings_profile_avatar_delete:
                deleteAvatar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, INTENT_REQUEST_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INTENT_REQUEST_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    onNewAvatarTaken(imageUri);
                }
        }
    }

    private void onNewAvatarTaken(Uri imageUri) {
        setUserAvatar(imageUri);
        mHasAvatarSet = true;
        getActivity().invalidateOptionsMenu();

        mListener.setBackdropAvatarFromUri(imageUri);
    }

    private void setUserAvatar(Uri imageUri) {
        mDeleteAvatar = false;
        Glide.with(this)
                .load(imageUri)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, Avatar.JPEG_COMPRESSION_RATE)
                .centerCrop()
                .into(new SimpleTarget<byte[]>(Avatar.WIDTH, Avatar.HEIGHT) {
                    @Override
                    public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                        mAvatar = resource;
                    }
                });
    }

    private void deleteAvatar() {
        mDeleteAvatar = true;
        mListener.setBackdropAvatar(null);
        mHasAvatarSet = false;
        getActivity().invalidateOptionsMenu();
    }

    /**
     * Checks whether the user made any changes and finishes if not. If yes show a dialog asking
     * the user if he/she wants to discard the changes.
     */
    public void checkForChangesAndExit() {
        if (changesWereMade()) {
            showDiscardChangesDialog();
        } else {
            finishEdit(NO_CHANGES);
        }
    }

    private boolean changesWereMade() {
        readFields();

        return !mEmail.equals(mCurrentEmail) && !ParseUtils.isTestUser(mCurrentUser) ||
                !mNickname.equals(mCurrentNickname) || !TextUtils.isEmpty(mPassword) ||
                mAvatar != null && !Arrays.equals(mAvatar, mCurrentUser.getAvatar());

    }

    private void readFields() {
        mEmail = mEditTextEmail.getText().toString().trim();
        mNickname = mEditTextNickname.getText().toString().trim();
        mPassword = mEditTextPassword.getText().toString();
        mPasswordRepeat = mEditTextPasswordRepeat.getText().toString();
    }

    private void showDiscardChangesDialog() {
        DiscardChangesDialogFragment discardChangesDialogFragment =
                new DiscardChangesDialogFragment();
        discardChangesDialogFragment.show(getFragmentManager(), DISCARD_CHANGES_DIALOG);
    }

    /**
     * Saves the changes made by the user to his/her profile if all fields contain valid values.
     */
    public void saveChanges() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        boolean fieldsAreComplete = true;
        readFields();

        if (Utils.emailIsValid(mEmail)) {
            mCurrentUser.setUsername(mEmail);
            mTextInputLayoutEmail.setErrorEnabled(false);
        } else {
            fieldsAreComplete = false;
            mTextInputLayoutEmail.setError(getString(R.string.error_email));
        }

        if (!TextUtils.isEmpty(mNickname)) {
            mCurrentUser.setNickname(mNickname);
            mTextInputLayoutNickname.setErrorEnabled(false);
        } else {
            fieldsAreComplete = false;
            mTextInputLayoutNickname.setError(getString(R.string.error_nickname));
        }

        if (!TextUtils.isEmpty(mPassword)) {
            if (mPassword.equals(mPasswordRepeat)) {
                mCurrentUser.setPassword(mPassword);
                mTextInputLayoutPasswordRepeat.setErrorEnabled(false);
            } else {
                fieldsAreComplete = false;
                mTextInputLayoutPasswordRepeat.setError(getString(R.string.error_login_signup_password_nomatch));
            }
        }

        if (mDeleteAvatar) {
            mCurrentUser.removeAvatar();
        } else if (mAvatar != null) {
            mCurrentUser.setAvatar(mAvatar);
        }

        if (fieldsAreComplete) {
            mCurrentUser.saveEventually();
            finishEdit(CHANGES_SAVED);
        }
    }

    /**
     * Sets the appropriate activity result and finishes the screen.
     *
     * @param editAction the action taken, used to set the activity results
     */
    public void finishEdit(@EditAction int editAction) {
        final Activity activity = getActivity();
        switch (editAction) {
            case CHANGES_SAVED:
                activity.setResult(Activity.RESULT_OK);
                break;
            case CHANGES_DISCARDED:
                activity.setResult(RESULT_CHANGES_DISCARDED);
                break;
            case NO_CHANGES:
                activity.setResult(Activity.RESULT_CANCELED);
        }

        ActivityCompat.finishAfterTransition(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Defines the interaction with the hosting {@link Activity}.
     * <p/>
     * Extends {@link BaseFragmentInteractionListener}.
     */
    public interface FragmentInteractionListener extends BaseFragment.BaseFragmentInteractionListener {
        /**
         * Sets the avatar images as the toolbar backdrop image.
         */
        void setBackdropAvatar(byte[] avatar);

        /**
         * Sets the avatar images as the toolbar backdrop image.
         */
        void setBackdropAvatarFromUri(Uri avatar);
    }
}
