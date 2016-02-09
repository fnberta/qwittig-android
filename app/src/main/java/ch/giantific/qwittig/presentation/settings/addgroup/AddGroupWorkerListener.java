/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.addgroup;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after a new group was successfully created or when the creation
 * failed.
 */
public interface AddGroupWorkerListener extends BaseWorkerListener {

    void setCreateGroupStream(@NonNull Single<User> single, @NonNull String workerTag);
}
