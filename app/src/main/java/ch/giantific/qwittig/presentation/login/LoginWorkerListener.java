/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.login;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after a successful login, a failed login or the reset of a
 * password.
 */
public interface LoginWorkerListener extends BaseWorkerListener {

    /**
     * Sets the user login stream.
     *
     * @param single    the {@link Single} emitting the result
     * @param workerTag the tag of the worker fragment
     * @param type      the action type taken
     */
    void setUserLoginStream(@NonNull Single<FirebaseUser> single, @NonNull String workerTag, @LoginWorker.Type int type);
}
