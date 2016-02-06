/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.viewmodels;

import android.databinding.Bindable;
import android.databinding.Observable;
import android.support.annotation.StringRes;

/**
 * Created by fabio on 30.01.16.
 */
public interface HeaderRowViewModel extends Observable {

    @StringRes
    @Bindable
    int getHeader();
}
