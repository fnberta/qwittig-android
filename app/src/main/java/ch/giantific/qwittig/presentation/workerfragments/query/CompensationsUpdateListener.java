/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;
import rx.Observable;

/**
 * Defines the actions to take after tasks are updated.
 */
public interface CompensationsUpdateListener extends BaseWorkerListener {
    /**
     * Sets the {@link Observable} that emits the compensations update stream
     *  @param observable the observable emitting the updates
     * @param workerTag  the tag of the worker fragment
     */
    void setCompensationsUpdateStream(@NonNull Observable<Compensation> observable,
                                      boolean paid, @NonNull String workerTag);
}
