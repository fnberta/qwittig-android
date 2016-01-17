/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;
import rx.Observable;

/**
 * Defines the actions to take after purchases are updated.
 */
public interface CompensationsQueryMoreListener extends BaseWorkerListener {
    /**
     * Sets the {@link Observable} that emits the query more compensations stream.
     *
     * @param observable the observable emitting the updates
     * @param workerTag  the tag of the worker fragment
     */
    void setCompensationsQueryMoreStream(@NonNull Observable<Compensation> observable,
                                         @NonNull String workerTag);
}
