/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.support.annotation.NonNull;

/**
 * Created by fabio on 13.01.16.
 */
public interface ViewInteraction<T> {
    /**
     * Attaches the view listener to the model for callbacks that require the framework (e.g.
     * context).
     *
     * @param view the view listener to attach
     */
    void attachView(@NonNull T view);

    /**
     * Detaches the view listener and cleans up any other references that might cause memory leaks.
     */
    void detachView();
}
