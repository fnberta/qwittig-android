/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels.rows;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Created by fabio on 20.01.16.
 */
public interface CardTopProgressViewModel extends Observable {

    @Bindable
    boolean isItemLoading();
}
