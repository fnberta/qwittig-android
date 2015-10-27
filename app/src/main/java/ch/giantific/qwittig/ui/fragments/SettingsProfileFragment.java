/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.ParseUser;

import java.util.Arrays;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.Avatar;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.activities.SettingsProfileActivity;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the profile details of the current user, allowing him/her to edit them.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsProfileFragment extends BaseFragment {

    private FragmentInteractionListener mListener;
    private TextInputLayout mTextInputLayoutEmail;
    private EditText mEditTextEmail;
    private TextInputLayout mTextInputLayoutNickname;
    private EditText mEditTextNickname;
    private TextInputLayout mTextInputLayoutPassword;
    private EditText mEditTextPassword;
    private TextInputLayout mTextInputLayoutPasswordRepeat;
    private EditText mEditTextPasswordRepeat;
    private User mCurrentUser;
    private String mCurrentEmail;
    private String mCurrentNickname;
    private String mEmail;
    private String mNickname;
    private String mPassword;
    private String mPasswordRepeat;
    private byte[] mAvatar;
    private boolean mDeleteAvatar;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_profile, container, false);

        mTextInputLayoutEmail = (TextInputLayout) rootView.findViewById(R.id.til_email);
        mEditTextEmail = mTextInputLayoutEmail.getEditText();
        mTextInputLayoutNickname = (TextInputLayout) rootView.findViewById(R.id.til_nickname);
        mEditTextNickname = mTextInputLayoutNickname.getEditText();
        mTextInputLayoutPassword = (TextInputLayout) rootView.findViewById(R.id.til_password);
        mEditTextPassword = mTextInputLayoutPassword.getEditText();
        mTextInputLayoutPasswordRepeat = (TextInputLayout) rootView.findViewById(R.id.til_password_repeat);
        mEditTextPasswordRepeat = mTextInputLayoutPasswordRepeat.getEditText();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCurrentUser = (User) ParseUser.getCurrentUser();
        mCurrentEmail = mCurrentUser.getUsername();
        mCurrentNickname = mCurrentUser.getNickname();
        if (!ParseUtils.isTestUser(mCurrentUser)) {
            mEditTextEmail.setText(mCurrentEmail);
        }
        mEditTextNickname.setText(mCurrentNickname);
    }

    /**
     * Returns whether the user made any changes.
     *
     * @return whether changes were made by the user
     */
    public boolean changesWereMade() {
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

    /**
     * Saves the changes made by the user to his/her profile if all fields contain valid values.
     */
    public void saveChanges() {
        final User currentUser = (User) ParseUser.getCurrentUser();

        if (ParseUtils.isTestUser(currentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        boolean fieldsAreComplete = true;
        readFields();

        if (Utils.emailIsValid(mEmail)) {
            currentUser.setUsername(mEmail);
            mTextInputLayoutEmail.setErrorEnabled(false);
        } else {
            fieldsAreComplete = false;
            mTextInputLayoutEmail.setError(getString(R.string.error_email));
        }

        if (!TextUtils.isEmpty(mNickname)) {
            currentUser.setNickname(mNickname);
            mTextInputLayoutNickname.setErrorEnabled(false);
        } else {
            fieldsAreComplete = false;
            mTextInputLayoutNickname.setError(getString(R.string.error_nickname));
        }

        if (!TextUtils.isEmpty(mPassword)) {
            if (mPassword.equals(mPasswordRepeat)) {
                currentUser.setPassword(mPassword);
                mTextInputLayoutPasswordRepeat.setErrorEnabled(false);
            } else {
                fieldsAreComplete = false;
                mTextInputLayoutPasswordRepeat.setError(getString(R.string.error_login_signup_password_nomatch));
            }
        }

        if (mDeleteAvatar) {
            currentUser.removeAvatar();
        } else if (mAvatar != null) {
            currentUser.setAvatar(mAvatar);
        }

        if (fieldsAreComplete) {
            currentUser.saveEventually();
            mListener.finishEdit(SettingsProfileActivity.CHANGES_SAVED);
        }
    }

    /**
     * Deletes the user's avatar image when the changes are saved.
     */
    public void deleteAvatar() {
        mDeleteAvatar = true;
    }

    /**
     * Sets the user's avatar iamge
     *
     * @param imageUri the {@link Uri} to the image
     */
    public void setAvatar(Uri imageUri) {
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
         * Indicates that a new avatar image should be taken.
         */
        void pickAvatar();

        /**
         * Indicates that the hosting {@link Activity} should finish with a specific result
         *
         * @param editAction the action to set the result with
         */
        void finishEdit(@SettingsProfileActivity.EditAction int editAction);
    }
}
