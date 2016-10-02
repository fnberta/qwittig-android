/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.di;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Provides an abstract base class for modules defining the instantiation of presenters.
 */
public abstract class BasePresenterModule {

    protected final Bundle savedState;

    public BasePresenterModule(@Nullable Bundle savedState) {
        this.savedState = savedState;
    }
}
