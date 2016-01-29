/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.domain.models.MessageAction;
import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;

/**
 * Defines the basic methods every view model contains.
 */
public interface ViewModel<T extends ViewModel.ViewListener>
        extends Observable, BaseWorkerListener, ViewInteraction<T> {

    /**
     * Saves the state of the view model in a bundle before recreation.
     *
     * @param outState the bundle to save the state in
     */
    void saveState(@NonNull Bundle outState);

    /**
     * Returns the current logged in user.
     *
     * @return the current logged in user
     */
    User getCurrentUser();

    /**
     * Loads the appropriate data for the newly set group.
     */
    void onNewGroupSet();

    interface ViewListener {
        boolean isNetworkAvailable();

        void showMessage(@StringRes int resId);

        void showMessage(@StringRes int resId, @NonNull String... args);

        void showMessageWithAction(@StringRes int resId, @NonNull MessageAction action);

        void showCreateGroupDialog(@StringRes int message);

        void removeWorker(@NonNull String workerTag);
    }
}
