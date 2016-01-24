/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.presentation.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import ch.giantific.qwittig.LocalBroadcastImpl;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.ui.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.AccountCreateDialogFragment;
import ch.giantific.qwittig.presentation.ui.fragments.dialogs.GroupCreateDialogFragment;
import ch.giantific.qwittig.presentation.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;

/**
 * Provides an abstract base class that sets up the {@link Toolbar} and commonly used methods.
 * <p/>
 * Subclass of {@link AppCompatActivity}.
 */
public abstract class BaseActivity<T extends ViewModel>
        extends AppCompatActivity
        implements BaseFragment.ActivityListener, BaseWorkerListener,
        AccountCreateDialogFragment.DialogInteractionListener,
        GroupCreateDialogFragment.DialogInteractionListener {

    public static final int INTENT_REQUEST_LOGIN = 1;
    public static final int INTENT_REQUEST_SETTINGS = 2;
    public static final int INTENT_REQUEST_PURCHASE_MODIFY = 3;
    public static final int INTENT_REQUEST_PURCHASE_DETAILS = 4;
    public static final int INTENT_REQUEST_SETTINGS_PROFILE = 5;
    public static final int INTENT_REQUEST_SETTINGS_GROUP_NEW = 6;
    public static final int INTENT_REQUEST_TASK_NEW = 7;
    public static final int INTENT_REQUEST_TASK_MODIFY = 8;
    public static final int INTENT_REQUEST_TASK_DETAILS = 9;
    Toolbar mToolbar;
    T mViewModel;

    @NonNull
    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            final int dataType = intent.getIntExtra(LocalBroadcastImpl.INTENT_DATA_TYPE, 0);
            handleLocalBroadcast(intent, dataType);
        }
    };

    @CallSuper
    void handleLocalBroadcast(Intent intent, int dataType) {
        // empty default implementation
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        setupToolbar();
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver,
                new IntentFilter(LocalBroadcastImpl.INTENT_FILTER_DATA_NEW));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver);
    }

    @Override
    public void onCreateNewAccountSelected() {
        final Context context = this;
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                // ignore possible exception, currentUser will always be null now
                final Intent intent = new Intent(context, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateGroupSelected() {
        final Intent intent = new Intent(this, SettingsGroupNewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onWorkerError(@NonNull String workerTag) {
        mViewModel.onWorkerError(workerTag);
    }
}
