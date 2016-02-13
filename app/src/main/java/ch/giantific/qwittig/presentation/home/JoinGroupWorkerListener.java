/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines actions to be taken during the invited group querying and joining process.
 */
public interface JoinGroupWorkerListener extends BaseWorkerListener {
    /**
     * Sets the join group stream.
     *
     * @param single    the single emitting the event
     * @param workerTag the tag of the worker fragment
     */
    void setJoinGroupStream(@NonNull Single<User> single, @NonNull String workerTag);
}
