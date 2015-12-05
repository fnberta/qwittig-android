/*
 * Copyright (c) 2015 Fabio Berta
 */

package ch.giantific.qwittig.workerfragments;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Provides an abstract class for a so-called headless {@link Fragment}, which does not contain
 * any UI elements and is retained across configuration changes. It is useful for encapsulating
 * background tasks.
 */
public abstract class BaseWorker extends Fragment {

    public BaseWorker() {
        // empty default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }
}
