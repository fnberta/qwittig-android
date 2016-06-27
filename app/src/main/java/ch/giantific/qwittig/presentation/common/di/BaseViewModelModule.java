/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.di;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Provides an abstract base class for modules defining the instantiation of view models.
 */
public abstract class BaseViewModelModule {

    protected final Bundle mSavedState;

    public BaseViewModelModule(@Nullable Bundle savedState) {
        mSavedState = savedState;
    }
}
