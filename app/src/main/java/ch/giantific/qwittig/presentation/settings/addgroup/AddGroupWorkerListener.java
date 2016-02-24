/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.Identity;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after a new group was successfully created or when the creation
 * failed.
 */
public interface AddGroupWorkerListener extends BaseWorkerListener {

    /**
     * Sets the create new group stream.
     *
     * @param single    the {@link Single} emitting the result
     * @param workerTag the tag of the headless worker fragment
     */
    void setCreateGroupStream(@NonNull Single<Identity> single, @NonNull String workerTag);
}
