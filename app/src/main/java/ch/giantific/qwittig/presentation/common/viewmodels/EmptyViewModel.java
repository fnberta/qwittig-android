/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Defines an observable view model for a screen with an empty view.
 */
public interface EmptyViewModel extends Observable {

    @Bindable
    boolean isEmpty();

    void setEmpty(boolean empty);
}
