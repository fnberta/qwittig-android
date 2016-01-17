/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.workerfragments.query;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.workerfragments.BaseWorkerListener;
import rx.Observable;

/**
 * Defines the actions to take after users are updated.
 */
public interface UserUpdateListener extends BaseWorkerListener {
    /**
     * Sets the {@link Observable} that emits the users update stream
     *
     * @param observable the observable emitting the updates
     * @param workerTag  the tag of the worker fragment
     */
    void setUsersUpdateStream(@NonNull Observable<User> observable, @NonNull String workerTag);
}
