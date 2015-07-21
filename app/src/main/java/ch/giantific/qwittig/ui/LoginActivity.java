package ch.giantific.qwittig.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.helper.AvatarHelper;
import ch.giantific.qwittig.helper.LoginHelper;
import ch.giantific.qwittig.data.models.ImageAvatar;
import ch.giantific.qwittig.data.parse.models.Installation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.ui.dialogs.ResetPasswordDialogFragment;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;


public class LoginActivity extends AppCompatActivity implements
        LoginFragment.FragmentInteractionListener,
        LoginSignUpFragment.FragmentInteractionListener,
        ResetPasswordDialogFragment.FragmentInteractionListener,
        AvatarHelper.HelperInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        LoginHelper.HelperInteractionListener {

    public static final String INTENT_URI_EMAIL = "intent_uri_email";
    public static final String INTENT_EXTRA_SIGN_UP = "intent_sign_up";
    private static final int INTENT_REQUEST_IMAGE = 1;
    private static final String LOGIN_FRAGMENT = "login_fragment";
    private static final String SIGN_UP_FRAGMENT = "sign_up_fragment";
    private static final String ASYNC_AVATAR_HELPER = "async_avatar_helper";
    private static final String LOGIN_HELPER = "login_helper";
    private static final String STATE_LOADING = "state_progress";
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private byte[] mAvatar;
    private View mViewMain;
    private View mViewProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Utils.isRunningLollipopAndHigher()) {
            setActivityTransition();
        }

        mViewMain = findViewById(R.id.container);
        mViewProgress = findViewById(R.id.ll_progress);

        Intent intent = getIntent();
        boolean showSignUpFragment = intent.getBooleanExtra(INTENT_EXTRA_SIGN_UP, false);
        String email = "";
        if (intent.hasExtra(INTENT_URI_EMAIL)) {
            email = intent.getStringExtra(INTENT_URI_EMAIL);
        }

        if (savedInstanceState == null) {
            Fragment fragment;
            String tag;

            if (showSignUpFragment) {
                fragment = LoginSignUpFragment.newInstance(email);
                tag = SIGN_UP_FRAGMENT;
            } else {
                fragment = LoginFragment.newInstance(email);
                tag = LOGIN_FRAGMENT;
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, tag)
                    .commit();
        } else {
            setLoading(savedInstanceState.getBoolean(STATE_LOADING));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        boolean isLoading = mViewProgress.getVisibility() == View.VISIBLE;
        outState.putBoolean(STATE_LOADING, isLoading);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setActivityTransition() {
        Transition transitionEnter = new Fade();
        getWindow().setEnterTransition(transitionEnter);

        Transition transitionExit = new Slide(Gravity.TOP);
        transitionExit.excludeTarget(android.R.id.statusBarBackground, true);
        transitionExit.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setExitTransition(transitionExit);
    }

    /**
     * Logs in a given user to the parse database.
     *
     * @param email
     * @param password
     */
    @Override
    public void logInUser(final String email, String password) {
        if (!Utils.isConnected(this)) {
            MessageUtils.showBasicSnackbar(mViewMain, getString(R.string.toast_no_connection));
            return;
        }

        setLoading(true);

        FragmentManager fragmentManager = getFragmentManager();
        LoginHelper loginHelper = findLoginHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginHelper == null) {
            loginHelper = LoginHelper.newInstance(email, password);

            fragmentManager.beginTransaction()
                    .add(loginHelper, LOGIN_HELPER)
                    .commit();
        }
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            hideSoftKeyboard();
            mViewProgress.setVisibility(View.VISIBLE);
            mViewMain.setVisibility(View.GONE);
        } else {
            mViewProgress.setVisibility(View.GONE);
            mViewMain.setVisibility(View.VISIBLE);
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();

        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private LoginHelper findLoginHelper(FragmentManager fragmentManager) {
        return (LoginHelper) fragmentManager.findFragmentByTag(LOGIN_HELPER);
    }

    @Override
    public void onParseError(ParseException e) {
        ParseErrorHandler.handleParseError(this, e);
        MessageUtils.showBasicSnackbar(mViewMain, ParseErrorHandler.getErrorMessage(this, e));
        setLoading(false);

        removeLoginHelper();
    }

    private void removeLoginHelper() {
        FragmentManager fragmentManager = getFragmentManager();
        LoginHelper loginHelper = findLoginHelper(fragmentManager);

        if (loginHelper != null) {
            fragmentManager.beginTransaction().remove(loginHelper).commit();
        }
    }

    @Override
    public void onLoginSucceeded(ParseUser parseUser) {
        addUserToInstallation(parseUser);
        finishLogin();
    }

    private void addUserToInstallation(ParseUser parseUser) {
        if (ParseUtils.isTestUser(parseUser)) {
            return;
        }

        User user = (User) parseUser;
        List<String> channels = user.getGroupIds();
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.addAllUnique(Installation.CHANNELS, channels);
        installation.put(Installation.USER, parseUser);
        installation.saveEventually();
    }

    private void finishLogin() {
        setResult(RESULT_OK);
        ActivityCompat.finishAfterTransition(this);
    }

    /**
     * Signs up a new user in the parse database.
     *
     * @param email
     * @param password
     * @param nickname
     */
    @Override
    public void createAccount(final String email, final String password, final String nickname) {
        if (!Utils.isConnected(this)) {
            MessageUtils.showBasicSnackbar(mViewMain, getString(R.string.toast_no_connection));
            return;
        }

        setLoading(true);

        FragmentManager fragmentManager = getFragmentManager();
        LoginHelper loginHelper = findLoginHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginHelper == null) {
            loginHelper = LoginHelper.newInstance(email, password, nickname, mAvatar);

            fragmentManager.beginTransaction()
                    .add(loginHelper, LOGIN_HELPER)
                    .commit();
        }
    }

    @Override
    public void resetPassword(String email) {
        if (!Utils.isConnected(this)) {
            MessageUtils.showBasicSnackbar(mViewMain, getString(R.string.toast_no_connection));
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        LoginHelper loginHelper = findLoginHelper(fragmentManager);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginHelper == null) {
            loginHelper = LoginHelper.newInstance(email);

            fragmentManager.beginTransaction()
                    .add(loginHelper, LOGIN_HELPER)
                    .commit();
        }
    }

    @Override
    public void onPasswordReset() {
        MessageUtils.showBasicSnackbar(mViewMain, getString(R.string.toast_reset_password_link));
    }

    @Override
    public void launchSignUpFragment(String email) {
        FragmentManager fragmentManager = getFragmentManager();
        LoginSignUpFragment loginSignUpFragment = LoginSignUpFragment.newInstance(email);
        fragmentManager.beginTransaction()
                .replace(R.id.container, loginSignUpFragment, SIGN_UP_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
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
                    .add(avatarHelper, ASYNC_AVATAR_HELPER)
                    .commit();
        }
    }

    private AvatarHelper findAvatarHelper(FragmentManager fragmentManager) {
        return (AvatarHelper)
                    fragmentManager.findFragmentByTag(ASYNC_AVATAR_HELPER);
    }

    @Override
    public void onPostExecute(ImageAvatar avatar) {
        if (avatar != null) {
            mAvatar = avatar.getByteArray();

            LoginSignUpFragment loginSignUpFragment = (LoginSignUpFragment)
                    getFragmentManager().findFragmentByTag(SIGN_UP_FRAGMENT);
            loginSignUpFragment.setAvatarImage(avatar);
        }

        // remote async fragment because we need a new one when the user clicks on the avatar again
        FragmentManager fragmentManager = getFragmentManager();
        AvatarHelper avatarHelper = findAvatarHelper(fragmentManager);
        if (avatarHelper != null) {
            fragmentManager.beginTransaction().remove(avatarHelper).commit();
        }
    }

    @Override
    public void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),

                ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        updateFragmentEmailField(emails);
    }

    private void updateFragmentEmailField(List<String> emails) {
        FragmentManager fragmentManager = getFragmentManager();

        LoginFragment loginFragment = (LoginFragment) fragmentManager
                .findFragmentByTag(LOGIN_FRAGMENT);

        LoginSignUpFragment loginSignUpFragment = (LoginSignUpFragment) fragmentManager
                .findFragmentByTag(SIGN_UP_FRAGMENT);

        if (loginFragment != null && loginFragment.isAdded()) {
            loginFragment.addEmailsToAutoComplete(emails);
        }

        if (loginSignUpFragment != null && loginSignUpFragment.isAdded()) {
            loginSignUpFragment.addEmailsToAutoComplete(emails);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            mAvatar = null;
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }
}
