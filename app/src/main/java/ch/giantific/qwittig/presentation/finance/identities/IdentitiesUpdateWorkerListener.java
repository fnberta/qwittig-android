/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance.identities;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Observable;

/**
 * Defines the actions to take after users are updated.
 */
public interface IdentitiesUpdateWorkerListener extends BaseWorkerListener {
    /**
     * Sets the {@link Observable} that emits the identities update stream
     *
     * @param observable the observable emitting the updates
     * @param workerTag  the tag of the worker fragment
     */
    void setIdentitiesUpdateStream(@NonNull Observable<Identity> observable, @NonNull String workerTag);
}
