/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.fragments;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;

/**
 * Provides an abstract base class for the login screen views.
 * <p/>
 * Subclass of {@link BaseFragment}.
 * <p/>
 * Implements {@link LoaderManager.LoaderCallbacks} to provide the user with email propositions
 * from this address book.
 */
public abstract class LoginEmailBaseFragment extends LoginBaseFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    static final String BUNDLE_EMAIL = "BUNDLE_EMAIL";
    private static final int PERMISSIONS_REQUEST_CONTACTS = 1;
    private static final String[] PROFILE_COLUMNS = new String[]{
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
    };
    private static final int COL_INDEX_ADDRESS = 0;
    TextInputLayout mTextInputLayoutEmail;
    AutoCompleteTextView mEditTextEmail;
    private String mEmail;

    public LoginEmailBaseFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mEmail = args.getString(BUNDLE_EMAIL);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.til_login_email_email);
        mEditTextEmail = (AutoCompleteTextView) mTextInputLayoutEmail.getEditText();
        if (!TextUtils.isEmpty(mEmail)) {
            mEditTextEmail.setText(mEmail);
        }
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

    @NonNull
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
    public void onLoadFinished(Loader<Cursor> loader, @NonNull Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(COL_INDEX_ADDRESS));
            cursor.moveToNext();
        }

        populateEmailField(emails);
    }

    private void populateEmailField(@NonNull List<String> emails) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, emails);

        mEditTextEmail.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }
}
