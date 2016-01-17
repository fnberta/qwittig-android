/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.reminder;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after a user was reminded or after the process failed.
 */
public interface CompensationReminderListener extends BaseWorkerListener {

    /**
     * Handles the successful reminder of a user to pay a compensation.
     *
     * @param single    the object id of the compensation to be paid
     * @param workerTag the tag of the worker fragment
     */
    void setCompensationReminderStream(@NonNull Single<String> single,
                                       @NonNull String compensationId,
                                       @NonNull String workerTag);
}
