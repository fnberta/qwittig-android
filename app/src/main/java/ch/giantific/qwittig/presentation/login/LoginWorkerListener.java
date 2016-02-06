/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.domain.models.parse.User;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after a successful login, a failed login or the reset of a
 * password.
 */
public interface LoginWorkerListener extends BaseWorkerListener {

    void setUserLoginStream(@NonNull Single<User> single, @NonNull String workerTag, @LoginWorker.Type int type);
}
