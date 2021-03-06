/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import javax.inject.Inject;

import ch.giantific.qwittig.R;
import ch.giantific.qwittig.data.helper.RemoteConfigHelper;
import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.views.BaseView;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;
import rx.subscriptions.CompositeSubscription;

/**
 * Provides an abstract base class that sets up the {@link Toolbar} and commonly used methods.
 * <p/>
 * Subclass of {@link AppCompatActivity}.
 */
public abstract class BaseActivity<T> extends AppCompatActivity
        implements BaseFragment.ActivityListener<T>, BaseWorkerListener, BaseView {

    protected final CompositeSubscription subscriptions = new CompositeSubscription();
    protected Toolbar toolbar;
    protected T component;
    @Inject
    RemoteConfigHelper configHelper;
    private List<BasePresenter> presenters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        injectDependencies(savedInstanceState);
        presenters = getPresenters();

        configHelper.fetchAndActivate();
    }

    protected abstract void injectDependencies(@Nullable Bundle savedInstanceState);

    @Override
    public T getComponent() {
        return component;
    }

    protected abstract List<BasePresenter> getPresenters();

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        setupToolbar();
    }

    @Override
    protected void onStart() {
        super.onStart();

        for (BasePresenter presenter : presenters) {
            if (presenter != null) {
                presenter.onViewVisible();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.clear();
        }

        for (BasePresenter presenter : presenters) {
            if (presenter != null) {
                presenter.onViewGone();
            }
        }
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
        }
    }

    @Override
    public boolean isNetworkAvailable() {
        return Utils.isNetworkAvailable(this);
    }

    @Override
    public void showMessage(@StringRes int resId) {
        Snackbar.make(toolbar, resId, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessage(@StringRes int resId, @NonNull Object... args) {
        Snackbar.make(toolbar, getString(resId, args), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action) {
        Snackbar.make(toolbar, resId, Snackbar.LENGTH_LONG)
                .setAction(action.getActionText(), action)
                .show();
    }

    @Override
    public void removeWorker(@NonNull String workerTag) {
        WorkerUtils.removeWorker(getSupportFragmentManager(), workerTag);
    }

    @Override
    public void onWorkerError(@NonNull String workerTag) {
        for (BasePresenter presenter : presenters) {
            if (presenter != null) {
                presenter.onWorkerError(workerTag);
            }
        }
    }
}
