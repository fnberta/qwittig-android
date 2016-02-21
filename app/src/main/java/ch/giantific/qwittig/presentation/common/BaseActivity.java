/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common;

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

import ch.giantific.qwittig.LocalBroadcastImpl;
import ch.giantific.qwittig.R;
import ch.giantific.qwittig.presentation.common.fragments.BaseFragment;
import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;

/**
 * Provides an abstract base class that sets up the {@link Toolbar} and commonly used methods.
 * <p/>
 * Subclass of {@link AppCompatActivity}.
 */
public abstract class BaseActivity<T extends ViewModel>
        extends AppCompatActivity
        implements BaseFragment.ActivityListener, BaseWorkerListener {

    public static final int INTENT_REQUEST_LOGIN = 1;
    public static final int INTENT_REQUEST_SETTINGS = 2;
    public static final int INTENT_REQUEST_PURCHASE_MODIFY = 3;
    public static final int INTENT_REQUEST_PURCHASE_DETAILS = 4;
    public static final int INTENT_REQUEST_SETTINGS_PROFILE = 5;
    public static final int INTENT_REQUEST_SETTINGS_ADD_GROUP = 6;
    public static final int INTENT_REQUEST_TASK_NEW = 7;
    public static final int INTENT_REQUEST_TASK_MODIFY = 8;
    public static final int INTENT_REQUEST_TASK_DETAILS = 9;
    protected Toolbar mToolbar;
    protected T mViewModel;

    @NonNull
    private final BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            final int dataType = intent.getIntExtra(LocalBroadcastImpl.INTENT_DATA_TYPE, 0);
            handleLocalBroadcast(intent, dataType);
        }
    };

    @CallSuper
    protected void handleLocalBroadcast(Intent intent, int dataType) {
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
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver,
                new IntentFilter(LocalBroadcastImpl.INTENT_FILTER_DATA_NEW));
    }

    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver);
    }

    @Override
    public void onWorkerError(@NonNull String workerTag) {
        mViewModel.onWorkerError(workerTag);
    }
}
