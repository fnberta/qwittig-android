/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Identity;
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
    void setUsersUpdateStream(@NonNull Observable<Identity> observable, @NonNull String workerTag);
}
