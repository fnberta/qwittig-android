/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings.groupusers.users;

import android.support.annotation.NonNull;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;
import rx.Single;

/**
 * Defines the actions to take after the invitation link was created.
 */
public interface InvitationLinkWorkerListener extends BaseWorkerListener {
    /**
     * Sets the invitation link creation result stream.
     *
     * @param single    the {@link Single} that emits the results
     * @param workerTag the tag of the worker fragment
     */
    void setInvitationLinkStream(@NonNull Single<String> single,
                                 @NonNull String workerTag);
}
