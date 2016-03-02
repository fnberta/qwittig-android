/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.StringRes;

/**
 * Defines an observable view model for a header row in a list.
 */
public interface HeaderRowViewModel extends Observable {

    @StringRes
    @Bindable
    int getHeader();
}
