/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.settings;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;

/**
 * Defines the actions to take after a new group was successfully created or when the creation
 * failed.
 */
public interface CreateGroupWorkerListener extends BaseWorkerListener {
    /**
     * Handles the successul creation of the new group.
     *
     * @param newGroup     the newly created {@link Group} object
     * @param invitingUser whether there is process ongoing to invite new users
     */
    void onNewGroupCreated(@NonNull Group newGroup, boolean invitingUser);

    /**
     * Handles the failure of the creation of the new group.
     *
     * @param errorMessage the error code thrown during the process
     */
    void onCreateNewGroupFailed(@StringRes int errorMessage);

    /**
     * Handles the successful invitation of the users to the new group.
     */
    void onUsersInvited();

    /**
     * Handles the case when the invitation of the users failed.
     *
     * @param errorMessage the error message thrown during the process
     */
    void onInviteUsersFailed(@StringRes int errorMessage);
}
