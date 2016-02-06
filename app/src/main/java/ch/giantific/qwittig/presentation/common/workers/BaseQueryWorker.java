/*
 * Copyright (c) 2016 Fabio Berta
 */

package ch.giantific.qwittig.presentation.common.workers;

import com.parse.ParseObject;

import java.util.List;

import ch.giantific.qwittig.domain.models.parse.Group;
import ch.giantific.qwittig.domain.models.parse.Identity;
import ch.giantific.qwittig.domain.models.parse.User;

/**
 * Provides an abstract base class for worker fragments whose task it is to query data from the
 * online data store.
 * <p/>
 * Subclass of {@link BaseWorker}.
 */
public abstract class BaseQueryWorker<T, S extends BaseWorkerListener>
        extends BaseWorker<T, S> {

    protected User mCurrentUser;
    protected Identity mCurrentIdentity;
    protected Group mCurrentGroup;
    protected List<ParseObject> mCurrentUserIdentities;

    /**
     * Returns if the current user's current group and groups are not null or empty.
     *
     * @return whether the current user's current group and groups are not null or empty
     */
    protected final boolean setCurrentGroups() {
        mCurrentUser = mUserRepo.getCurrentUser();

        if (mCurrentUser != null) {
            mCurrentIdentity = mCurrentUser.getCurrentIdentity();
            mCurrentGroup = mCurrentIdentity.getGroup();
            mCurrentUserIdentities = mCurrentUser.getIdentities();
        }

        return mCurrentUser != null && mCurrentGroup != null && !mCurrentUserIdentities.isEmpty();
    }
}
