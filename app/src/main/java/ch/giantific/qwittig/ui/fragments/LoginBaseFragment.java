/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.List;

import ch.giantific.qwittig.ParseErrorHandler;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.workerfragments.account.LoginWorker;
import ch.giantific.qwittig.domain.models.parse.Installation;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.utils.WorkerUtils;
import ch.giantific.qwittig.utils.ParseUtils;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.ViewUtils;

/**
 * Provides an abstract base class for the login screen views.
 * <p/>
 * Subclass of {@link BaseFragment}.
 * <p/>
 * Implements {@link LoaderManager.LoaderCallbacks} to provide the user with email propositions
 * from this address book.
 */
public abstract class LoginBaseFragment extends BaseFragment {

    static final String LOGIN_WORKER = "LOGIN_WORKER";
    private static final String STATE_LOADING = "STATE_LOADING";
    View mViewMain;
    private View mViewProgress;

    public LoginBaseFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewMain = view.findViewById(R.id.ll_login_main);
        mViewProgress = view.findViewById(R.id.ll_login_progress);

        if (savedInstanceState != null) {
            setLoading(savedInstanceState.getBoolean(STATE_LOADING));
        }
    }

    final void setLoading(boolean isLoading) {
        if (isLoading) {
            ViewUtils.hideSoftKeyboard(getActivity());
            mViewProgress.setVisibility(View.VISIBLE);
            mViewMain.setVisibility(View.GONE);
        } else {
            mViewProgress.setVisibility(View.GONE);
            mViewMain.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        boolean isLoading = mViewProgress.getVisibility() == View.VISIBLE;
        outState.putBoolean(STATE_LOADING, isLoading);
    }

    final void loginWithEmailWithWorker(@NonNull final String email, @NonNull String password) {
        if (!Utils.isConnected(getActivity())) {
            Snackbar.make(mViewMain, R.string.toast_no_connection, Snackbar.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment loginWorker = WorkerUtils.findWorker(fragmentManager, LOGIN_WORKER);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (loginWorker == null) {
            loginWorker = LoginWorker.newInstanceLogin(email, password);

            fragmentManager.beginTransaction()
                    .add(loginWorker, LOGIN_WORKER)
                    .commit();
        }
    }

    /**
     * Handles a failed login attempt. Passes error to generic Parse error handler, hides the
     * progress bar and removes the worker fragment.
     *
     * @param errorCode the error code of th exception thrown during the login attempt
     */
    public void onLoginFailed(int errorCode) {
        final Activity context = getActivity();
        ParseErrorHandler.handleParseError(context, errorCode);
        Snackbar.make(mViewMain,
                ParseErrorHandler.getErrorMessage(context, errorCode), Snackbar.LENGTH_LONG).show();
        setLoading(false);

        WorkerUtils.removeWorker(getFragmentManager(), LOGIN_WORKER);
    }

    /**
     * Adds user to the installation object and finishes the login process after a successful
     * login attempt
     *
     * @param parseUser the now authenticated ParseUser
     */
    public void onLoggedIn(@NonNull ParseUser parseUser) {
        addUserToInstallation(parseUser);
        ActivityCompat.finishAfterTransition(getActivity());
    }

    private void addUserToInstallation(@NonNull ParseUser parseUser) {
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
}
