/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.workers;

import android.support.annotation.NonNull;

/**
 * Defines the base error handler for headless worker fragments.
 */
public interface BaseWorkerListener {

    void onWorkerError(@NonNull String workerTag);
}
