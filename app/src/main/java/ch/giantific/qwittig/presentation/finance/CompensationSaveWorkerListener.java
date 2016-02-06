/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.finance;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after tasks are updated.
 */
public interface CompensationSaveWorkerListener extends BaseWorkerListener {
    /**
     * Sets the {@link Single} that emits the saving of a compensation.
     *
     * @param single    the observable emitting the save stream
     * @param workerTag the tag of the worker fragment
     */
    void setCompensationSaveStream(@NonNull Single<Compensation> single,
                                   @NonNull String workerTag);
}
