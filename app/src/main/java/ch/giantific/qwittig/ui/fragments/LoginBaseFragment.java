package ch.giantific.qwittig.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.parse.models.Installation;
import ch.giantific.qwittig.data.parse.models.User;
import ch.giantific.qwittig.utils.MessageUtils;
import ch.giantific.qwittig.utils.ParseErrorHandler;
import ch.giantific.qwittig.utils.ParseUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public abstract class LoginBaseFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOGIN_HELPER = "login_helper";
    private static final int PERMISSIONS_REQUEST_CONTACTS = 1;
    private static final String[] PROFILE_COLUMNS = new String[]{
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
    };
    private static final int COL_INDEX_ADDRESS = 0;
    private static final String STATE_LOADING = "state_progress";
    private View mViewMain;
    private View mViewProgress;
    AutoCompleteTextView mEditTextEmail;

    public LoginBaseFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewMain = view.findViewById(R.id.ll_login_main);
        mViewProgress = view.findViewById(R.id.ll_login_progress);

        if (savedInstanceState != null) {
            setLoading(savedInstanceState.getBoolean(STATE_LOADING));
        }
    }

    final void setLoading(boolean isLoading) {
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
        Activity activity = getActivity();

        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();

        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        boolean isLoading = mViewProgress.getVisibility() == View.VISIBLE;
        outState.putBoolean(STATE_LOADING, isLoading);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (permissionsAreGranted()) {
            initLoader();
        }
    }

    private boolean permissionsAreGranted() {
        List<String> permissionsToRequest = new ArrayList<>();

        int hasGetAccountsPerm = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS);
        if (hasGetAccountsPerm != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.GET_ACCOUNTS);
        }

        int hasReadContactsPerm = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS);
        if (hasReadContactsPerm != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS);
        }

        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(
                    new String[permissionsToRequest.size()]);
            FragmentCompat.requestPermissions(this, permissionsArray, PERMISSIONS_REQUEST_CONTACTS);

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CONTACTS:
                boolean allPermissionsGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }

                if (allPermissionsGranted) {
                    initLoader();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initLoader() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),

                // select columns
                PROFILE_COLUMNS,

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
            emails.add(cursor.getString(COL_INDEX_ADDRESS));
            cursor.moveToNext();
        }

        populateEmailField(emails);
    }

    private void populateEmailField(List<String> emails) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, emails);

        mEditTextEmail.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    /**
     * Handles a failed login attempt. Passes error to generic Parse error handler, hides the
     * progress bar and removes the helper fragment.
     * @param e the ParseException thrown during the login attempt
     */
    public void onLoginFailed(ParseException e) {
        ParseErrorHandler.handleParseError(getActivity(), e);
        MessageUtils.showBasicSnackbar(getView(), ParseErrorHandler.getErrorMessage(getActivity(), e));
        setLoading(false);

        removeHelper(LOGIN_HELPER);
    }

    /**
     * Adds user to the installation object and finishes the login process after a successful
     * login attempt
     * @param parseUser the now authenticated ParseUser
     */
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
        getActivity().setResult(Activity.RESULT_OK);
        ActivityCompat.finishAfterTransition(getActivity());
    }
}
