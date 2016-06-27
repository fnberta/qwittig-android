/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Defines an observable view model for a screen that either shows the content or a spinning
 * progress bar.
 */
public interface LoadingViewModel extends Observable {

    @Bindable
    boolean isLoading();

    void setLoading(boolean isLoading);
}
