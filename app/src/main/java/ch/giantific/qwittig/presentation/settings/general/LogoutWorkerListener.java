/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.general;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after the user was logged out or the logout failed
 */
public interface LogoutWorkerListener extends BaseWorkerListener {
    /**
     * Sets the logout stream.
     *
     * @param single    the {@link Single} emitting the result
     * @param workerTag the tag of the headless worker fragment
     */
    void setLogoutStream(@NonNull Single<User> single, @NonNull String workerTag);
}
