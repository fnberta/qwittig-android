/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.workers;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after an email user was deleted or the password reset.
 */
public interface EmailUserWorkerListener extends BaseWorkerListener {
    /**
     * Sets the email user stream.
     *
     * @param single    the {@link Single} emitting the result
     * @param workerTag the tag of the headless worker fragment
     */
    void setEmailUserStream(@NonNull Single<Void> single, @NonNull String workerTag);
}
