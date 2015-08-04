package ch.giantific.qwittig.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.models.ImageAvatar;
import ch.giantific.qwittig.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginSignUpFragment extends Fragment {

    private static final String BUNDLE_EMAIL = "email";
    private static final int NICKNAME_MAX_CHARACTERS = 10;
    private String mEmail;
    private TextInputLayout mTextInputLayoutEmail;
    private AutoCompleteTextView mEditTextEmail;
    private TextInputLayout mTextInputLayoutPassword;
    private EditText mEditTextPassword;
    private TextInputLayout mTextInputLayoutPasswordRepeat;
    private EditText mEditTextPasswordRepeat;
    private TextInputLayout mTextInputLayoutNickname;
    private EditText mEditTextNickname;
    private ImageView mImageButtonAvatar;
    private Button mButtonSignUp;

    private FragmentInteractionListener mListener;

    public LoginSignUpFragment() {
    }

    public static LoginSignUpFragment newInstance(String email) {
        LoginSignUpFragment fragment = new LoginSignUpFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mEmail = getArguments().getString(BUNDLE_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login_sign_up, container, false);

        mTextInputLayoutEmail = (TextInputLayout) rootView.findViewById(R.id.til_login_signup_email);
        mEditTextEmail = (AutoCompleteTextView) mTextInputLayoutEmail.getEditText();
        mTextInputLayoutPassword = (TextInputLayout) rootView.findViewById(R.id.til_login_signup_password);
        mEditTextPassword = mTextInputLayoutPassword.getEditText();
        mTextInputLayoutPasswordRepeat = (TextInputLayout) rootView.findViewById(R.id.til_login_signup_password_repeat);
        mEditTextPasswordRepeat = mTextInputLayoutPasswordRepeat.getEditText();
        mTextInputLayoutNickname = (TextInputLayout) rootView.findViewById(R.id.til_login_signup_nickname);
        mEditTextNickname = mTextInputLayoutNickname.getEditText();
        mImageButtonAvatar = (ImageView) rootView.findViewById(R.id.ibt_login_signup_avatar);
        mButtonSignUp = (Button) rootView.findViewById(R.id.bt_login_signup);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mEmail.length() > 0) {
            mEditTextEmail.setText(mEmail);
        }

        mImageButtonAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.pickAvatar();
            }
        });
        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }

    public void setAvatarImage(ImageAvatar avatar) {
        mImageButtonAvatar.setAlpha(1f);
        mImageButtonAvatar.setImageDrawable(avatar.getRoundedDrawable());
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
            mListener.createAccount(email, password, nickname);
        } else {
            focusView.requestFocus();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListener.populateAutoComplete();
    }

    public void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEditTextEmail.setAdapter(adapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentInteractionListener {
        void pickAvatar();

        void createAccount(String email, String password, String nickname);

        void populateAutoComplete();
    }

}
