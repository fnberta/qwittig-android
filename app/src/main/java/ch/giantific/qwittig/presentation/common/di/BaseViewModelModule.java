/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.di;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ch.giantific.qwittig.presentation.common.viewmodels.ViewModel;

/**
 * Created by fabio on 16.01.16.
 */
public abstract class BaseViewModelModule<T extends ViewModel.ViewListener> {

    protected final Bundle mSavedState;
    protected final T mView;

    public BaseViewModelModule(@Nullable Bundle savedState, @NonNull T view) {
        mSavedState = savedState;
        mView = view;
    }
}
