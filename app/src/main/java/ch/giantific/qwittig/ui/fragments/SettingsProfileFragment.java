/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import ch.berta.fabio.fabprogress.FabProgress;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.utils.AvatarUtils;
import ch.giantific.qwittig.workerfragments.account.UnlinkThirdPartyWorker;
import ch.giantific.qwittig.ui.fragments.dialogs.DiscardChangesDialogFragment;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.utils.parse.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the profile details of the current user, allowing him/her to edit them.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class SettingsProfileFragment extends BaseFragment {

    @IntDef({Activity.RESULT_OK, Activity.RESULT_CANCELED, RESULT_CHANGES_DISCARDED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EditResult {}
    public static final int RESULT_CHANGES_DISCARDED = 2;
    private static final String LOG_TAG = SettingsProfileFragment.class.getSimpleName();
    private static final String DISCARD_CHANGES_DIALOG = "DISCARD_CHANGES_DIALOG";
    private static final int INTENT_REQUEST_IMAGE = 1;
    private static final String STATE_IS_SAVING = "STATE_IS_SAVING";
    private static final String STATE_UNLINK_THIRD_PARTY = "STATE_UNLINK_THIRD_PARTY";
    private static final String UNLINK_WORKER = "UNLINK_WORKER";
    private View mViewPassword;
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
    private boolean mIsFacebookUser;
    private boolean mIsGoogleUser;
    private boolean mUnlinkThirdPartyLogin;
    private boolean mIsSaving;
    private Snackbar mSnackbarSetPassword;

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
        updateCurrentUserAndGroup();
        mCurrentEmail = mCurrentUser.getUsername();
        mCurrentNickname = mCurrentUser.getNickname();
        mIsFacebookUser = mCurrentUser.isFacebookUser();
        mIsGoogleUser = mCurrentUser.isGoogleUser();

        if (savedInstanceState != null) {
            mUnlinkThirdPartyLogin = savedInstanceState.getBoolean(STATE_UNLINK_THIRD_PARTY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.til_email);
        mEditTextEmail = mTextInputLayoutEmail.getEditText();
        if (!ParseUtils.isTestUser(mCurrentUser)) {
            mEditTextEmail.setText(mCurrentEmail);
        }

        mTextInputLayoutNickname = (TextInputLayout) view.findViewById(R.id.til_nickname);
        mEditTextNickname = mTextInputLayoutNickname.getEditText();
        mEditTextNickname.setText(mCurrentNickname);

        setupPasswordViews(view);
        checkAvatar();
    }

    private void setupPasswordViews(View view) {
        mViewPassword = view.findViewById(R.id.ll_password);

        mTextInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.til_password);
        mEditTextPassword = mTextInputLayoutPassword.getEditText();
        mEditTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mUnlinkThirdPartyLogin) {
                    if (TextUtils.isEmpty(s.toString())) {
                        showSetPasswordSnackbar();
                    } else {
                        mSnackbarSetPassword.dismiss();
                    }
                }

                validatePassword();
            }
        });

        mTextInputLayoutPasswordRepeat = (TextInputLayout) view.findViewById(R.id.til_password_repeat);
        mEditTextPasswordRepeat = mTextInputLayoutPasswordRepeat.getEditText();
        mEditTextPasswordRepeat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validatePassword();
            }
        });

        togglePasswordVisibility();
    }

    private void validatePassword() {
        if (!mEditTextPassword.getText().toString().equals(mEditTextPasswordRepeat.getText().toString())) {
            if (!mTextInputLayoutPasswordRepeat.isErrorEnabled()) {
                mTextInputLayoutPasswordRepeat.setError(getString(R.string.error_login_signup_password_nomatch));
            }
        } else {
            mTextInputLayoutPasswordRepeat.setErrorEnabled(false);
        }
    }

    private void togglePasswordVisibility() {
        if (mUnlinkThirdPartyLogin) {
            mViewPassword.setVisibility(View.VISIBLE);
            mTextInputLayoutPasswordRepeat.setVisibility(View.VISIBLE);
        } else if (mIsFacebookUser || mIsGoogleUser) {
            mViewPassword.setVisibility(View.GONE);
            mTextInputLayoutPasswordRepeat.setVisibility(View.GONE);
        }
    }

    private void checkAvatar() {
        byte[] avatar = mCurrentUser.getAvatar();
        mHasAvatarSet = avatar != null;
        getActivity().invalidateOptionsMenu();
        mListener.setBackdropAvatar(avatar);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mIsSaving = savedInstanceState.getBoolean(STATE_IS_SAVING);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_UNLINK_THIRD_PARTY, mUnlinkThirdPartyLogin);
        outState.putBoolean(STATE_IS_SAVING, mIsSaving);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings_profile, menu);
        MenuItem deleteAvatar = menu.findItem(R.id.action_settings_profile_avatar_delete);
        deleteAvatar.setVisible(mHasAvatarSet);
        MenuItem unlinkFacebook = menu.findItem(R.id.action_settings_profile_unlink_facebook);
        unlinkFacebook.setVisible(mIsFacebookUser && !mUnlinkThirdPartyLogin);
        MenuItem unlinkGoogle = menu.findItem(R.id.action_settings_profile_unlink_google);
        unlinkGoogle.setVisible(mIsGoogleUser && !mUnlinkThirdPartyLogin);
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
            case R.id.action_settings_profile_unlink_facebook:
                // fall through
            case R.id.action_settings_profile_unlink_google:
                unlinkThirdPartyLogin();
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
                .toBytes(Bitmap.CompressFormat.JPEG, AvatarUtils.JPEG_COMPRESSION_RATE)
                .centerCrop()
                .into(new SimpleTarget<byte[]>(AvatarUtils.WIDTH, AvatarUtils.HEIGHT) {
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

    private void unlinkThirdPartyLogin() {
        mUnlinkThirdPartyLogin = true;
        getActivity().invalidateOptionsMenu();
        togglePasswordVisibility();
        mTextInputLayoutPassword.requestFocus();
        showSetPasswordSnackbar();
    }

    private void showSetPasswordSnackbar() {
        mSnackbarSetPassword = Snackbar.make(mEditTextEmail, R.string.toast_unlink_password_required,
                Snackbar.LENGTH_INDEFINITE);
        mSnackbarSetPassword.show();
    }

    /**
     * Saves the changes made by the user to his profile if all fields contain valid values.
     */
    public void saveChanges() {
        if (ParseUtils.isTestUser(mCurrentUser)) {
            mListener.showAccountCreateDialog();
            return;
        }

        readValues();

        boolean valuesChanged = false;
        if (emailIsValid()) {
            mCurrentUser.setUsername(mEmail);
            valuesChanged = true;
        }
        if (nicknameIsValid()) {
            mCurrentUser.setNickname(mNickname);
            valuesChanged = true;
        }
        if (passwordIsNewAndValid()) {
            mCurrentUser.setPassword(mPassword);
            valuesChanged = true;
        }

        if (mDeleteAvatar) {
            mCurrentUser.removeAvatar();
            valuesChanged = true;
        } else if (mAvatar != null) {
            mCurrentUser.setAvatar(mAvatar);
            valuesChanged = true;
        }

        if (valuesChanged) {
            mIsSaving = true;

            if (!mUnlinkThirdPartyLogin) {
                mCurrentUser.saveEventually();
                finishEdit(Activity.RESULT_OK);
            } else if (mIsGoogleUser) {
                mListener.startProgressAnim();
                unlinkThirdPartyWithWorker(UnlinkThirdPartyWorker.UNLINK_GOOGLE);
            } else if (mIsFacebookUser) {
                mListener.startProgressAnim();
                unlinkThirdPartyWithWorker(UnlinkThirdPartyWorker.UNLINK_FACEBOOK);
            }
        }
    }

    private void readValues() {
        mEmail = mEditTextEmail.getText().toString().trim();
        mNickname = mEditTextNickname.getText().toString().trim();
        mPassword = mEditTextPassword.getText().toString();
        mPasswordRepeat = mEditTextPasswordRepeat.getText().toString();
    }

    private boolean emailIsValid() {
        if (Utils.emailIsValid(mEmail)) {
            mTextInputLayoutEmail.setErrorEnabled(false);
            return true;
        } else {
            mTextInputLayoutEmail.setError(getString(R.string.error_email));
            return false;
        }
    }

    private boolean nicknameIsValid() {
        if (!TextUtils.isEmpty(mNickname)) {
            mTextInputLayoutNickname.setErrorEnabled(false);
            return true;
        } else {
            mTextInputLayoutNickname.setError(getString(R.string.error_nickname));
            return false;
        }
    }

    private boolean passwordIsNewAndValid() {
        if (Utils.passwordIsValid(mPassword)) {
            if (mPassword.equals(mPasswordRepeat)) {
                mTextInputLayoutPasswordRepeat.setErrorEnabled(false);
                return true;
            } else {
                mTextInputLayoutPasswordRepeat.setError(getString(R.string.error_login_signup_password_nomatch));
                return false;
            }
        } else {
            return false;
        }
    }

    private void unlinkThirdPartyWithWorker(@UnlinkThirdPartyWorker.UnlinkAction int unlinkAction) {
        if (!Utils.isConnected(getActivity())) {
            Snackbar.make(mTextInputLayoutNickname, getString(R.string.toast_no_connection),
                    Snackbar.LENGTH_LONG).show();;
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment unlinkWorker = WorkerUtils.findWorker(fragmentManager, UNLINK_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (unlinkWorker == null) {
            unlinkWorker = UnlinkThirdPartyWorker.newInstance(unlinkAction);

            fragmentManager.beginTransaction()
                    .add(unlinkWorker, UNLINK_WORKER)
                    .commit();
        }
    }

    /**
     * Shows the user the error message and removes the retained worker fragment and loading
     * indicators.
     *
     * @param errorMessage the error message from the exception thrown in the process
     */
    public void onThirdPartyUnlinkFailed(@StringRes int errorMessage) {
        WorkerUtils.removeWorker(getFragmentManager(), UNLINK_WORKER);
        Snackbar.make(mTextInputLayoutNickname, errorMessage, Snackbar.LENGTH_LONG).show();

        mIsSaving = false;
    }

    /**
     * Checks whether the user made any changes and finishes if not. If yes show a dialog asking
     * the user if he/she wants to discard the changes.
     */
    public void checkForChangesAndExit() {
        if (mIsSaving) {
            Snackbar.make(mTextInputLayoutNickname, R.string.toast_saving_profile,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        if (changesWereMade()) {
            showDiscardChangesDialog();
        } else {
            finishEdit(Activity.RESULT_CANCELED);
        }
    }

    private boolean changesWereMade() {
        readValues();

        return !mEmail.equals(mCurrentEmail) && !ParseUtils.isTestUser(mCurrentUser) ||
                !mNickname.equals(mCurrentNickname) || !TextUtils.isEmpty(mPassword) ||
                mAvatar != null && !Arrays.equals(mAvatar, mCurrentUser.getAvatar());

    }

    private void showDiscardChangesDialog() {
        DiscardChangesDialogFragment discardChangesDialogFragment =
                new DiscardChangesDialogFragment();
        discardChangesDialogFragment.show(getFragmentManager(), DISCARD_CHANGES_DIALOG);
    }

    /**
     * Sets the appropriate activity result and finishes the screen.
     *
     * @param editResult the action taken, used to set the activity results
     */
    public void finishEdit(@EditResult int editResult) {
        final Activity activity = getActivity();
        activity.setResult(editResult);
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

        /**
         * Handles the start of the loading animation of the {@link FabProgress}.
         */
        void startProgressAnim();
    }
}
