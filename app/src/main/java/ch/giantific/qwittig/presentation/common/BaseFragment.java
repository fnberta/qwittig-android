/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import javax.inject.Inject;

import ch.giantific.qwittig.presentation.common.presenters.BasePresenter;
import ch.giantific.qwittig.presentation.common.presenters.BaseViewListener;
import ch.giantific.qwittig.utils.Utils;
import ch.giantific.qwittig.utils.WorkerUtils;

/**
 * Provides an abstract base class for fragments to house commonly used methods.
 * <p/>
 * Currently only incorporates a base interface for communication with an activity.
 * <p/>
 * Subclass of {@link Fragment}.
 */
public abstract class BaseFragment<U, T extends BasePresenter, S extends BaseFragment.ActivityListener<U>>
        extends Fragment implements BaseViewListener {

    protected S activity;
    @Inject
    protected T presenter;

    public BaseFragment() {
        // required empty constructor
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activity = (S) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ActivityListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        injectDependencies(activity.getComponent());
    }

    protected abstract void injectDependencies(@NonNull U component);

    @Override
    public void onStart() {
        super.onStart();

        presenter.onViewVisible();
    }

    @Override
    public void onStop() {
        super.onStop();

        presenter.onViewGone();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    @Override
    public boolean isNetworkAvailable() {
        return Utils.isNetworkAvailable(getActivity());
    }

    @Override
    public void showMessage(@StringRes int resId) {
        Snackbar.make(getSnackbarView(), resId, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessage(@StringRes int resId, @NonNull Object... args) {
        Snackbar.make(getSnackbarView(), getString(resId, args), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action) {
        Snackbar.make(getSnackbarView(), resId, Snackbar.LENGTH_LONG)
                .setAction(action.getActionText(), action)
                .show();
    }

    protected abstract View getSnackbarView();

    @Override
    public void removeWorker(@NonNull String workerTag) {
        WorkerUtils.removeWorker(getFragmentManager(), workerTag);
    }

    /**
     * Default interaction listener with the hosting activity for subclasses to extend from.
     */
    public interface ActivityListener<T> {
        T getComponent();
    }
}
