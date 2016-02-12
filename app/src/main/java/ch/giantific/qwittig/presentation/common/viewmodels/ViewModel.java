/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.utils.MessageAction;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;

/**
 * Defines the basic methods every view model contains.
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
    void onStart();

    /**
     * Loads the appropriate data for the newly set group.
     */
    void onIdentitySelected();

    /**
     * Cleans up any long living tasks, e.g. RxJava subscriptions, in order to allow the view model
     * and the view it references to be garbage collected.
     */
    void onStop();

    interface ViewListener {
        boolean isNetworkAvailable();

        void showMessage(@StringRes int resId);

        void showMessage(@StringRes int resId, @NonNull String... args);

        void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action);

        void removeWorker(@NonNull String workerTag);
    }
}
