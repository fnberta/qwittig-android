/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.save;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.Compensation;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;
import rx.Observable;
import rx.Single;

/**
 * Defines the actions to take after tasks are updated.
 */
public interface CompensationSaveListener extends BaseWorkerListener {
    /**
     * Sets the {@link Single} that emits the saving of a compensation.
     *
     * @param observable the observable emitting the save stream
     * @param workerTag  the tag of the worker fragment
     */
    void setCompensationSaveStream(@NonNull Single<Compensation> observable,
                                   @NonNull String workerTag);
}
