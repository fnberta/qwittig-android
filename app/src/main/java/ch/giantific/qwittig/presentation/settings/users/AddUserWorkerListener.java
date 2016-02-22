/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.users;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the action to take after users were invited or the invitation failed.
 */
public interface AddUserWorkerListener extends BaseWorkerListener {
    /**
     * Sets the {@link Single} that emits the user invitation.
     *
     * @param single    the single emitting the user invitation
     * @param workerTag the tag of the worker fragment
     */
    void setAddUserStream(@NonNull Single<String> single, @NonNull String workerTag);
}
