/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.di.modules;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by fabio on 16.01.16.
 */
public abstract class BaseViewModelModule {

    Bundle mSavedState;

    public BaseViewModelModule(@Nullable Bundle savedState) {
        mSavedState = savedState;
    }

}
