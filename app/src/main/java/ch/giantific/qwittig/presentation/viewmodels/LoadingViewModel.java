/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.viewmodels;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Created by fabio on 10.01.16.
 */
public interface LoadingViewModel extends Observable {

    @Bindable
    boolean isLoading();

    void setLoading(boolean isLoading);
}
