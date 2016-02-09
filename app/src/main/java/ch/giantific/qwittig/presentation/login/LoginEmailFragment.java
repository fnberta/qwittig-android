/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.databinding.FragmentLoginEmailBinding;
import ch.giantific.qwittig.di.components.DaggerLoginEmailComponent;
import ch.giantific.qwittig.di.modules.LoginEmailViewModelModule;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.fragments.EmailPromptDialogFragment;
import ch.giantific.qwittig.utils.ViewUtils;

/**
 * Displays the login screen asking the user for the username and password.
 * <p/>
 * Subclass of {@link BaseFragment}.
 */
public class LoginEmailFragment extends BaseFragment<LoginEmailViewModel, LoginEmailFragment.ActivityListener>
        implements LoaderManager.LoaderCallbacks<Cursor>, LoginEmailViewModel.ViewListener {

    private static final int PERMISSIONS_REQUEST_CONTACTS = 1;
    private static final String[] PROFILE_COLUMNS = new String[]{
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
    };
    private static final int COL_INDEX_ADDRESS = 0;
    private FragmentLoginEmailBinding mBinding;

    public LoginEmailFragment() {
        // required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerLoginEmailComponent.builder()
                .loginEmailViewModelModule(new LoginEmailViewModelModule(savedInstanceState, this))
                .build()
                .inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentLoginEmailBinding.inflate(inflater, container, false);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
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

        final int hasGetAccountsPerm = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.GET_ACCOUNTS);
        if (hasGetAccountsPerm != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.GET_ACCOUNTS);
        }

        final int hasReadContactsPerm = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS);
        if (hasReadContactsPerm != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS);
        }

        if (!permissionsToRequest.isEmpty()) {
            final String[] permissionsArray = permissionsToRequest.toArray(
                    new String[permissionsToRequest.size()]);
            requestPermissions(permissionsArray, PERMISSIONS_REQUEST_CONTACTS);

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
        final List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(COL_INDEX_ADDRESS));
            cursor.moveToNext();
        }

        populateEmailField(emails);
    }

    private void populateEmailField(@NonNull List<String> emails) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, emails);
        mBinding.etLoginEmail.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

    @Override
    protected void setViewModelToActivity() {
        mActivity.setEmailViewModel(mViewModel);
    }

    @Override
    protected View getSnackbarView() {
        return mBinding.btLoginEmailLogin;
    }

    @Override
    public void loadEmailLoginWorker(@NonNull String email, @NonNull String password) {
        LoginWorker.attachEmailLoginInstance(getFragmentManager(), email, password);
    }

    @Override
    public void loadEmailSignUpWorker(@NonNull String email, @NonNull String password) {
        LoginWorker.attachEmailSignUpInstance(getFragmentManager(), email, password);
    }

    @Override
    public void loadResetPasswordWorker(@NonNull String email) {
        LoginWorker.attachResetPasswordInstance(getFragmentManager(), email);
    }

    @Override
    public void showResetPasswordDialog(@NonNull String email) {
        EmailPromptDialogFragment.display(getFragmentManager(),
                R.string.dialog_login_reset_password_title,
                R.string.dialog_login_reset_password_message,
                R.string.dialog_positive_reset,
                email);
    }

    @Override
    public void hideKeyboard() {
        ViewUtils.hideSoftKeyboard(getActivity());
    }

    @Override
    public void finishScreen(int result) {
        final FragmentActivity activity = getActivity();
        activity.setResult(result);
        ActivityCompat.finishAfterTransition(activity);
    }

    public interface ActivityListener extends BaseFragment.ActivityListener {
        void setEmailViewModel(@NonNull LoginEmailViewModel viewModel);
    }
}
