/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.workers;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the google actions related to a user.
 */
public interface GoogleUserWorkerListener extends BaseWorkerListener {
    /**
     * Sets the google user stream.
     *
     * @param single    the {@link Single} emitting the result
     * @param workerTag the tag of the headless worker fragment
     */
    void setGoogleUserStream(@NonNull Single<Void> single, @NonNull String workerTag);
}
