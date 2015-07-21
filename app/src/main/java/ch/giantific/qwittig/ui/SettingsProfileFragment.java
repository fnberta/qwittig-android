package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.ParseUser;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ImageAvatar;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsProfileFragment extends Fragment {

    private TextInputLayout mTextInputLayoutEmail;
    private EditText mEditTextEmail;
    private TextInputLayout mTextInputLayoutNickname;
    private EditText mEditTextNickname;
    private TextInputLayout mTextInputLayoutPassword;
    private EditText mEditTextPassword;
    private TextInputLayout mTextInputLayoutPasswordRepeat;
    private EditText mEditTextPasswordRepeat;
    private ImageView mImageViewAvatar;

    private User mCurrentUser;
    private String mCurrentEmail;
    private String mCurrentNickname;
    private String mEmail;
    private String mNickname;
    private String mPassword;
    private String mPasswordRepeat;


    private FragmentInteractionListener mListener;

    public SettingsProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        mImageViewAvatar = (ImageView) rootView.findViewById(R.id.iv_avatar);

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

        byte[] avatarByteArray = mCurrentUser.getAvatar();
        Drawable avatar = ImageAvatar.getRoundedAvatar(getActivity(), avatarByteArray, true);
        mImageViewAvatar.setImageDrawable(avatar);
        mImageViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.pickAvatar();
            }
        });
    }

    public boolean changesWereMade() {
        readFields();

        return !mEmail.equals(mCurrentEmail) && !ParseUtils.isTestUser(mCurrentUser) ||
                !mNickname.equals(mCurrentNickname) || !TextUtils.isEmpty(mPassword);

    }

    private void readFields() {
        mEmail = mEditTextEmail.getText().toString().trim();
        mNickname = mEditTextNickname.getText().toString().trim();
        mPassword = mEditTextPassword.getText().toString();
        mPasswordRepeat = mEditTextPasswordRepeat.getText().toString();
    }

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
        } else {
            fieldsAreComplete = false;
            mTextInputLayoutEmail.setError(getString(R.string.error_email));
        }

        if (!TextUtils.isEmpty(mNickname)) {
            currentUser.setNickname(mNickname);
        } else {
            fieldsAreComplete = false;
            mTextInputLayoutNickname.setError(getString(R.string.error_nickname));
        }

        if (!TextUtils.isEmpty(mPassword)) {
            if (mPassword.equals(mPasswordRepeat)) {
                currentUser.setPassword(mPassword);
            } else {
                fieldsAreComplete = false;
                mTextInputLayoutPasswordRepeat.setError(getString(R.string.error_login_signup_password_nomatch));
            }
        }

        byte[] avatar = mListener.getAvatar();
        if (avatar != null) {
            currentUser.setAvatar(avatar);
        }

        if (fieldsAreComplete) {
            currentUser.saveEventually();
            mListener.finishEdit(SettingsProfileActivity.CHANGES_SAVED);
        }
    }

    public void setAvatarImage(ImageAvatar avatar) {
        mImageViewAvatar.setImageDrawable(avatar.getRoundedDrawable());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void pickAvatar();

        void finishEdit(@SettingsProfileActivity.EditAction int editAction);

        byte[] getAvatar();

        void showAccountCreateDialog();
    }
}
