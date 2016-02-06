/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.home;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.parse.ParseObject;

import ch.giantific.qwittig.presentation.common.workers.BaseWorkerListener;

/**
 * Defines actions to be taken during the invited group querying and joining process.
 */
public interface InvitedGroupWorkerListener extends BaseWorkerListener {
    /**
     * Handles the successful query of the group the user is invited to
     *
     * @param group the queried group
     */
    void onInvitedGroupQueried(@NonNull ParseObject group);

    /**
     * Handles the failure to query of the group the user is invited to.
     *
     * @param errorMessage the error message from the exception thrown during the process
     */
    void onInvitedGroupQueryFailed(@StringRes int errorMessage);

    /**
     * Handles the case when user's email was removed from the users invited to get group
     */
    void onEmailNotValid();

    /**
     * Handles the case when the user successfully joined the new group
     */
    void onUserJoinedGroup();

    /**
     * Handles the failure to join the new group.
     *
     * @param errorMessage the error message from the exception thrown during the process
     */
    void onUserJoinGroupFailed(@StringRes int errorMessage);
}
