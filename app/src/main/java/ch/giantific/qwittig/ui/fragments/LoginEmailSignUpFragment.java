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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.domain.models.Avatar;
import ch.giantific.qwittig.workerfragments.account.LoginWorker;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * Displays the sign up screen asking the user for an email, password, nickname and optionally an
 * avatar image.
 * <p/>
 * Subclass of {@link LoginBaseFragment}.
 */
public class LoginEmailSignUpFragment extends LoginEmailBaseFragment {

    private static final int NICKNAME_MAX_CHARACTERS = 10;
    private static final int INTENT_REQUEST_IMAGE = 1;
    @Nullable
    private TextInputLayout mTextInputLayoutPassword;
    private EditText mEditTextPassword;
    private TextInputLayout mTextInputLayoutPasswordRepeat;
    private EditText mEditTextPasswordRepeat;
    private TextInputLayout mTextInputLayoutNickname;
    private EditText mEditTextNickname;
    private ImageView mImageButtonAvatar;
    private Button mButtonSignUp;
    private byte[] mAvatar;

    public LoginEmailSignUpFragment() {
    }

    /**
     * Returns a new instance of a {@link LoginEmailSignUpFragment} with an email address as an argument.
     *
     * @param email the email to be used for the sign up
     * @return a new instance of a {@link LoginEmailSignUpFragment}
     */
    @NonNull
    public static LoginEmailSignUpFragment newInstance(String email) {
        LoginEmailSignUpFragment fragment = new LoginEmailSignUpFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_email_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.til_login_signup_password);
        mEditTextPassword = mTextInputLayoutPassword.getEditText();
        mTextInputLayoutPasswordRepeat = (TextInputLayout) view.findViewById(R.id.til_login_signup_password_repeat);
        mEditTextPasswordRepeat = mTextInputLayoutPasswordRepeat.getEditText();
        mTextInputLayoutNickname = (TextInputLayout) view.findViewById(R.id.til_login_signup_nickname);
        mEditTextNickname = mTextInputLayoutNickname.getEditText();

        mImageButtonAvatar = (ImageView) view.findViewById(R.id.ibt_login_signup_avatar);
        mImageButtonAvatar.setImageDrawable(Avatar.getFallbackDrawable(getActivity(), true, true));
        mImageButtonAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAvatar();
            }
        });

        mButtonSignUp = (Button) view.findViewById(R.id.bt_login_signup);
        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }

    private void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, INTENT_REQUEST_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LoginEmailSignUpFragment.INTENT_REQUEST_IMAGE &&
                resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            setAvatar(imageUri);
        }
    }

    private void setAvatar(Uri imageUri) {
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

        Glide.with(this)
                .load(imageUri)
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(mImageButtonAvatar) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        view.setImageDrawable(Avatar.getRoundedDrawable(getActivity(), resource, true));
                    }
                });
    }

    private void signUpUser() {
        View focusView = null;
        boolean fieldsAreComplete = true;

        String email = mEditTextEmail.getText().toString().trim();
        String password = mEditTextPassword.getText().toString();
        String passwordRepeat = mEditTextPasswordRepeat.getText().toString();
        String nickname = mEditTextNickname.getText().toString().trim();

        if (!Utils.emailIsValid(email)) {
            fieldsAreComplete = false;
            mTextInputLayoutEmail.setError(getString(R.string.error_email));
            focusView = mEditTextEmail;
        } else {
            mTextInputLayoutEmail.setErrorEnabled(false);
        }

        if (TextUtils.isEmpty(password)) {
            fieldsAreComplete = false;
            mTextInputLayoutPassword.setError(getString(R.string.error_login_password));
            focusView = mEditTextPassword;
        } else {
            mTextInputLayoutPassword.setErrorEnabled(false);

            if (!password.equals(passwordRepeat)) {
                mTextInputLayoutPasswordRepeat.setError(getString(R.string.error_login_signup_password_nomatch));
                focusView = mEditTextPasswordRepeat;
                fieldsAreComplete = false;
            } else {
                mTextInputLayoutPasswordRepeat.setErrorEnabled(false);
            }
        }

        if (TextUtils.isEmpty(nickname)) {
            fieldsAreComplete = false;
            mTextInputLayoutNickname.setError(getString(R.string.error_nickname));
            focusView = mEditTextNickname;
        } else if (nickname.length() > NICKNAME_MAX_CHARACTERS) {
            fieldsAreComplete = false;
            mTextInputLayoutNickname.setError(getString(R.string.error_nickname_length));
            focusView = mEditTextNickname;
        } else {
            mTextInputLayoutNickname.setErrorEnabled(false);
        }

        if (fieldsAreComplete) {
            createAccountWithWorker(email, password, nickname);
        } else {
            focusView.requestFocus();
        }
    }

    private void createAccountWithWorker(@NonNull final String email,
                                         @NonNull final String password,
                                         @NonNull final String nickname) {
        if (!Utils.isConnected(getActivity())) {
            Snackbar.make(mButtonSignUp, R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginWorker = WorkerUtils.findWorker(fragmentManager, LoginEmailFragment.LOGIN_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginWorker == null) {
            loginWorker = LoginWorker.newInstanceSignUp(email, password, nickname, mAvatar);
            fragmentManager.beginTransaction()
                    .add(loginWorker, LoginEmailFragment.LOGIN_WORKER)
                    .commit();
        }
    }
}
