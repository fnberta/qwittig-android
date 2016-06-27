/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.profile;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.User;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import ch.giantific.qwittig.presentation.settings.profile.UnlinkThirdPartyWorker.ProfileAction;
import rx.Single;

/**
 * Defines the actions to take after a successful login, a failed login or the reset of a
 * password.
 */
public interface UnlinkThirdPartyWorkerListener extends BaseWorkerListener {
    /**
     * Sets the profile action stream.
     *
     * @param single    the {@link Single} emitting the result
     * @param workerTag the tag of the headless worker fragment
     * @param action    the action type
     */
    void setUnlinkActionStream(@NonNull Single<User> single, @NonNull String workerTag,
                               @ProfileAction int action);
}
