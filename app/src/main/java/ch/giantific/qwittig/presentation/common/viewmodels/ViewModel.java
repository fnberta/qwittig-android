/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import ch.giantific.qwittig.utils.MessageAction;

/**
 * Defines an observable view model for a screen.
 */
public interface ViewModel
        extends Observable, BaseWorkerListener {

    /**
     * Saves the state of the view model in a bundle before recreation.
     *
     * @param outState the bundle to save the state in
     */
    void saveState(@NonNull Bundle outState);

    /**
     * Sets up RxJava composite subscriptions and loads the data for the view.
     */
    void onViewVisible();

    /**
     * Cleans up any long living tasks, e.g. RxJava subscriptions, in order to allow the view model
     * and the view it references to be garbage collected.
     */
    void onViewGone();

    /**
     * Defines the interaction with the attached view.
     */
    interface ViewListener {
        boolean isNetworkAvailable();

        void showMessage(@StringRes int resId);

        void showMessage(@StringRes int resId, @NonNull Object... args);

        void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action);

        void removeWorker(@NonNull String workerTag);
    }
}
