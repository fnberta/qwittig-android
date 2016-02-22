/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Defines an observable view model for a progress bar on top of a card view.
 */
public interface CardTopProgressViewModel extends Observable {

    @Bindable
    boolean isItemLoading();
}
